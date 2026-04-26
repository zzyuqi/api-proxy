package com.zhongzhuan.proxy.controller;

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

    public RelayController(RouteService routeService,
                           RelayService relayService,
                           ApiKeyService apiKeyService,
                           RateLimitService rateLimitService,
                           LogService logService,
                           UserService userService) {
        this.routeService = routeService;
        this.relayService = relayService;
        this.apiKeyService = apiKeyService;
        this.rateLimitService = rateLimitService;
        this.logService = logService;
        this.userService = userService;
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
                // Check token balance
                if (proxyUser.getTokenBalance() - proxyUser.getTokenUsed() <= 0) {
                    return error(402, "Token 余额不足，请联系管理员分配额度");
                }
            } else if (apiKeyValue != null && !apiKeyValue.isEmpty()) {
                ApiKey apiKey = apiKeyService.validateKey(apiKeyValue).orElse(null);
                if (apiKey == null) {
                    return error(403, "API Key 无效或已禁用");
                }
                apiKeyId = apiKey.getId();
            } else {
                return error(401, "缺少认证信息 (X-User-Token 或 X-API-Key)");
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

        // Token usage tracking — parse MiniMax response for usage.total_tokens
        long tokensUsed = 0;
        if (proxyUser != null && response.getBody() != null && response.getStatusCode().is2xxSuccessful()) {
            tokensUsed = extractTokenUsage(response.getBody());
            if (tokensUsed > 0) {
                userService.deductTokens(proxyUser.getId(), tokensUsed,
                        "请求 " + fullPath + " 消耗 " + tokensUsed + " tokens");
            }
        }

        // Async log
        RequestLog log = new RequestLog();
        log.setRouteId(route.getId());
        log.setApiKeyId(apiKeyId);
        log.setRequestPath(fullPath);
        log.setRequestMethod(method);
        log.setResponseStatus(response.getStatusCode().value());
        log.setResponseTimeMs(System.currentTimeMillis() - startTime);
        log.setClientIp(clientIp);
        logService.saveLog(log);

        return response;
    }

    private long extractTokenUsage(byte[] responseBody) {
        try {
            String json = new String(responseBody, StandardCharsets.UTF_8);
            // Simple JSON parsing for {"usage":{"total_tokens": N }}
            int usageIdx = json.indexOf("\"usage\"");
            if (usageIdx < 0) return 0;
            int totalIdx = json.indexOf("\"total_tokens\"", usageIdx);
            if (totalIdx < 0) {
                totalIdx = json.indexOf("\"totalTokens\"", usageIdx);
            }
            if (totalIdx < 0) return 0;
            int colonIdx = json.indexOf(':', totalIdx);
            int endIdx = json.indexOf(',', colonIdx);
            if (endIdx < 0) endIdx = json.indexOf('}', colonIdx);
            if (colonIdx > 0 && endIdx > colonIdx) {
                return Long.parseLong(json.substring(colonIdx + 1, endIdx).trim());
            }
        } catch (Exception e) {
            // Parsing failure is non-critical
        }
        return 0;
    }

    private ResponseEntity<byte[]> error(int status, String msg) {
        return ResponseEntity.status(status).body(("{\"error\":\"" + msg + "\"}").getBytes());
    }

    private boolean shouldForwardHeader(String name) {
        String lower = name.toLowerCase();
        return !lower.equals("host")
                && !lower.equals("content-length")
                && !lower.equals("transfer-encoding")
                && !lower.equals("connection");
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
