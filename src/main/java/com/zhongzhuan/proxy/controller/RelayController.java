package com.zhongzhuan.proxy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhongzhuan.proxy.model.ApiKey;
import com.zhongzhuan.proxy.model.ProxyUser;
import com.zhongzhuan.proxy.model.RequestLog;
import com.zhongzhuan.proxy.model.Route;
import com.zhongzhuan.proxy.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@RestController
public class RelayController {

    private final RouteService routeService;
    private final RelayService relayService;
    private final ApiKeyService apiKeyService;
    private final RateLimitService rateLimitService;
    private final LogService logService;
    private final UserService userService;
    private final UserRateLimitService userRateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 系统提示词，控制回复简洁高效
    private static final String SYSTEM_PROMPT = "回复要精简高效，去除冗余内容和注释，直接给出最优解。";

    public RelayController(RouteService routeService,
                           RelayService relayService,
                           ApiKeyService apiKeyService,
                           RateLimitService rateLimitService,
                           LogService logService,
                           UserService userService,
                           UserRateLimitService userRateLimitService) {
        this.routeService = routeService;
        this.relayService = relayService;
        this.apiKeyService = apiKeyService;
        this.rateLimitService = rateLimitService;
        this.logService = logService;
        this.userService = userService;
        this.userRateLimitService = userRateLimitService;
    }

    @RequestMapping("/api/proxy/**")
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIp(request);

        // Match route
        Route route = routeService.matchRoute(fullPath, method);
        if (route == null) {
            return error(404, "未匹配到路由规则: " + fullPath);
        }

        // Auth check — support X-User-Token, Authorization: Bearer (user), and X-API-Key (legacy)
        String userToken = request.getHeader("X-User-Token");
        String apiKeyValue = request.getHeader("X-API-Key");
        Long apiKeyId = null;
        ProxyUser proxyUser = null;

        // Also check Authorization: Bearer <token> (OpenAI SDK compatibility)
        if (userToken == null || userToken.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                userToken = authHeader.substring(7);
            }
        }

        if (route.getRequiresAuth()) {
            if (userToken != null && !userToken.isEmpty()) {
                proxyUser = userService.authenticateByToken(userToken).orElse(null);
                if (proxyUser == null) {
                    return error(403, "用户 Token 无效或已禁用");
                }
                // Check account expiration
                if (proxyUser.getExpireAt() != null &&
                    proxyUser.getExpireAt().isBefore(java.time.LocalDateTime.now())) {
                    return error(403, "账号已过期，请联系管理员");
                }
                // Check token expiration (1 hour)
                if (proxyUser.getLastActiveAt() != null &&
                    proxyUser.getLastActiveAt().plusHours(1).isBefore(java.time.LocalDateTime.now())) {
                    return error(403, "登录已过期，请重新登录");
                }
                // Update last active time
                userService.updateLastActive(proxyUser.getId());
                // Check request quota
                if (proxyUser.getRequestCount() - proxyUser.getRequestsUsed() <= 0) {
                    return error(402, "请求次数已用完，请联系管理员分配额度");
                }
            } else if (apiKeyValue != null && !apiKeyValue.isEmpty()) {
                ApiKey apiKey = apiKeyService.validateKey(apiKeyValue).orElse(null);
                if (apiKey == null) {
                    return error(403, "API Key 无效或已禁用");
                }
                apiKeyId = apiKey.getId();
                // If API Key is bound to a user, use that user for quota deduction
                if (apiKey.getUserId() != null) {
                    proxyUser = userService.getUser(apiKey.getUserId());
                    if (proxyUser == null) {
                        return error(403, "API Key 关联的用户不存在");
                    }
                    if (!proxyUser.getEnabled()) {
                        return error(403, "用户已被禁用");
                    }
                    // Check account expiration
                    if (proxyUser.getExpireAt() != null &&
                        proxyUser.getExpireAt().isBefore(java.time.LocalDateTime.now())) {
                        return error(403, "账号已过期，请联系管理员");
                    }
                    // Check token expiration (1 hour)
                    if (proxyUser.getLastActiveAt() != null &&
                        proxyUser.getLastActiveAt().plusHours(1).isBefore(java.time.LocalDateTime.now())) {
                        return error(403, "登录已过期，请重新登录");
                    }
                    // Update last active time
                    userService.updateLastActive(proxyUser.getId());
                    // Check user request quota
                    if (proxyUser.getRequestCount() - proxyUser.getRequestsUsed() <= 0) {
                        return error(402, "请求次数已用完，请联系管理员分配额度");
                    }
                }
            } else {
                return error(401, "缺少认证信息 (X-User-Token 或 X-API-Key)");
            }
        }

        // Check concurrent limit
        if (proxyUser != null && proxyUser.getConcurrentLimit() != null && proxyUser.getConcurrentLimit() > 0) {
            if (!userRateLimitService.tryAcquireConcurrent(proxyUser.getId(), proxyUser.getConcurrentLimit())) {
                return error(429, "并发请求数超限，当前最大并发: " + proxyUser.getConcurrentLimit());
            }
        }

        // Check hourly limit
        if (proxyUser != null && proxyUser.getHourlyLimit() != null && proxyUser.getHourlyLimit() > 0) {
            if (!userRateLimitService.tryAcquireHourly(proxyUser.getId(), proxyUser.getHourlyLimit())) {
                return error(429, "请求频率超限，每小时最大请求数: " + proxyUser.getHourlyLimit());
            }
        }

        // Rate limit check
        if (route.getRateLimit() != null && route.getRateLimit() > 0) {
            String rateKey = route.getId() + ":" + clientIp;
            if (!rateLimitService.tryAcquire(rateKey, route.getRateLimit())) {
                return error(429, "请求频率超过限制，每分钟最多" + route.getRateLimit() + "次");
            }
        }

        // Build target URL
        String targetUrl = routeService.buildTargetUrl(route, fullPath);

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (shouldForwardHeader(name)) {
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.add(name, values.nextElement());
                }
            }
        }

        // Inject MiniMax API key if this route targets minimaxi.com
        if (route.getTargetUrl() != null && route.getTargetUrl().contains("minimaxi")) {
            headers.set("Authorization", "Bearer sk-cp-cJkyJVlMic0hBesGKgLMOVshRJb6dW_HSaBwmvjpOaxD-BbhJnDowTT5VLdrw9bZzKgH0gIyl9TUBl0u-emczJwwJKtHLwYcsT4ZY0RqWDqKgFFDjPaDhzc");
        }

        // Read body
        byte[] body;
        try {
            body = request.getInputStream().readAllBytes();
        } catch (IOException e) {
            body = new byte[0];
        }

        // Inject system prompt to control response length (for chat completions)
        body = injectSystemPrompt(body);

        // Append query string if present
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl += "?" + queryString;
        }

        // Forward request
        long startTime = System.currentTimeMillis();
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        ResponseEntity<byte[]> response = relayService.forwardRequest(
                route, targetUrl, httpMethod, headers, body);

        // Release concurrent permit
        if (proxyUser != null && proxyUser.getConcurrentLimit() != null && proxyUser.getConcurrentLimit() > 0) {
            userRateLimitService.releaseConcurrent(proxyUser.getId());
        }

        // Request count tracking — increment usage on successful/failure response from proxy target
        // Count all responses except 5xx server errors
        if (proxyUser != null && response.getStatusCode().value() < 500) {
            userService.incrementRequestUsage(proxyUser.getId(),
                    "请求 " + fullPath);
        }

        // Async log
        RequestLog log = new RequestLog();
        log.setRouteId(route.getId());
        log.setApiKeyId(apiKeyId);
        log.setUserId(proxyUser != null ? proxyUser.getId() : null);
        log.setUsername(proxyUser != null ? proxyUser.getUsername() : null);
        log.setRequestPath(fullPath);
        log.setRequestMethod(method);
        log.setResponseStatus(response.getStatusCode().value());
        log.setResponseTimeMs(System.currentTimeMillis() - startTime);
        log.setClientIp(clientIp);
        logService.saveLog(log);

        return response;
    }

    /**
     * 在请求体中的 messages 数组最前面插入系统提示词，并限制上下文消息数量
     */
    private byte[] injectSystemPrompt(byte[] body) {
        if (body == null || body.length == 0) {
            return body;
        }

        try {
            String jsonStr = new String(body, StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(jsonStr);

            JsonNode messagesNode = root.get("messages");
            if (messagesNode == null || !messagesNode.isArray()) {
                return body;
            }

            ArrayNode messages = (ArrayNode) messagesNode;
            if (messages.size() == 0) {
                return body;
            }

            // 提取系统消息（如果存在）
            ObjectNode systemMsg = null;
            ArrayNode nonSystemMessages = objectMapper.createArrayNode();

            for (JsonNode msg : messages) {
                if ("system".equals(msg.path("role").asText())) {
                    if (systemMsg == null) {
                        systemMsg = (ObjectNode) msg.deepCopy();
                    }
                } else {
                    nonSystemMessages.add(msg.deepCopy());
                }
            }

            // 限制消息数量（保留最近的消息）
            int maxMessages = 20;
            ArrayNode limitedMessages = objectMapper.createArrayNode();
            int start = Math.max(0, nonSystemMessages.size() - maxMessages);
            for (int i = start; i < nonSystemMessages.size(); i++) {
                limitedMessages.add(nonSystemMessages.get(i));
            }

            // 构建新消息数组：系统提示 + 限制后的消息
            ArrayNode newMessages = objectMapper.createArrayNode();
            ObjectNode promptMsg = objectMapper.createObjectNode();
            promptMsg.put("role", "system");
            promptMsg.put("content", SYSTEM_PROMPT);
            newMessages.add(promptMsg);
            limitedMessages.forEach(newMessages::add);

            ((ObjectNode) root).set("messages", newMessages);
            return objectMapper.writeValueAsBytes(root);
        } catch (Exception e) {
            return body;
        }
    }

    private ResponseEntity<byte[]> error(int status, String msg) {
        return ResponseEntity.status(status).body(("{\"error\":\"" + msg + "\"}").getBytes());
    }

    private boolean shouldForwardHeader(String name) {
        String lower = name.toLowerCase();
        return !lower.equals("host")
                && !lower.equals("content-length")
                && !lower.equals("transfer-encoding")
                && !lower.equals("connection")
                && !lower.equals("x-api-key");
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
