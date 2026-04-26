package com.zhongzhuan.proxy.controller;

import com.zhongzhuan.proxy.dto.*;
import com.zhongzhuan.proxy.model.*;
import com.zhongzhuan.proxy.repository.*;
import com.zhongzhuan.proxy.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RouteService routeService;
    private final ApiKeyService apiKeyService;
    private final RequestLogRepository requestLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AdminController(RouteService routeService,
                           ApiKeyService apiKeyService,
                           RequestLogRepository requestLogRepository,
                           AdminUserRepository adminUserRepository,
                           PasswordEncoder passwordEncoder,
                           UserService userService) {
        this.routeService = routeService;
        this.apiKeyService = apiKeyService;
        this.requestLogRepository = requestLogRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // ==================== 系统初始化 ====================

    @PostMapping("/init")
    public ResponseEntity<?> initAdmin(@RequestBody Map<String, String> body) {
        if (adminUserRepository.count() > 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "管理员已存在，不能重复初始化"));
        }

        String username = body.getOrDefault("username", "admin");
        String password = body.getOrDefault("password", "admin123");

        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("ADMIN");
        adminUserRepository.save(admin);

        return ResponseEntity.ok(Map.of(
                "message", "管理员创建成功",
                "username", username
        ));
    }

    // ==================== 路由管理 ====================

    @PostMapping("/routes")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        Route route = new Route();
        route.setName(request.getName());
        route.setPathPattern(request.getPathPattern());
        route.setTargetUrl(request.getTargetUrl());
        if (request.getMethod() != null) route.setMethod(request.getMethod().toUpperCase());
        route.setRequiresAuth(request.getRequiresAuth());
        route.setRateLimit(request.getRateLimit());
        route.setCacheTtl(request.getCacheTtl());
        route.setAllowedModels(request.getAllowedModels());
        route.setStatus(request.getStatus());
        route.setUserVisible(request.getUserVisible() != null ? request.getUserVisible() : true);

        Route saved = routeService.saveRoute(route);
        return ResponseEntity.status(HttpStatus.CREATED).body(RouteResponse.from(saved));
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> listRoutes() {
        List<RouteResponse> routes = routeService.listRoutes().stream()
                .map(RouteResponse::from)
                .toList();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> getRoute(@PathVariable Long id) {
        Route route = routeService.getRoute(id);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(RouteResponse.from(route));
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id,
                                                     @Valid @RequestBody RouteRequest request) {
        Route route = routeService.getRoute(id);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        route.setName(request.getName());
        route.setPathPattern(request.getPathPattern());
        route.setTargetUrl(request.getTargetUrl());
        if (request.getMethod() != null) {
            route.setMethod(request.getMethod().toUpperCase());
        } else {
            route.setMethod(null);
        }
        route.setRequiresAuth(request.getRequiresAuth());
        route.setRateLimit(request.getRateLimit());
        route.setCacheTtl(request.getCacheTtl());
        route.setAllowedModels(request.getAllowedModels());
        route.setStatus(request.getStatus());
        if (request.getUserVisible() != null) {
            route.setUserVisible(request.getUserVisible());
        }

        Route saved = routeService.saveRoute(route);
        return ResponseEntity.ok(RouteResponse.from(saved));
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== API Key 管理 ====================

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyResponse> createApiKey(@Valid @RequestBody ApiKeyRequest request) {
        ApiKey apiKey = apiKeyService.createKey(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiKeyResponse.from(apiKey));
    }

    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys() {
        List<ApiKeyResponse> keys = apiKeyService.listKeys().stream()
                .map(ApiKeyResponse::from)
                .toList();
        return ResponseEntity.ok(keys);
    }

    @PutMapping("/api-keys/{id}")
    public ResponseEntity<ApiKeyResponse> updateApiKey(
            @PathVariable Long id,
            @Valid @RequestBody ApiKeyRequest request) {
        return apiKeyService.updateKey(id, request.getName(), request.getEnabled())
                .map(ApiKeyResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api-keys/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        apiKeyService.deleteKey(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 日志查询 ====================

    @GetMapping("/logs")
    public ResponseEntity<Page<RequestLog>> queryLogs(LogQueryParams params) {
        Specification<RequestLog> spec = Specification.where(null);

        if (params.getRouteId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("routeId"), params.getRouteId()));
        }
        if (params.getStatusCode() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("responseStatus"), params.getStatusCode()));
        }

        Page<RequestLog> logs = requestLogRepository.findAll(spec, params.toPageable());
        return ResponseEntity.ok(logs);
    }

    // ==================== 用户管理 ====================

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        ProxyUser user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getRole(),
                request.getTokenBalance()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userService.listUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        ProxyUser user = userService.getUser(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        ProxyUser user = userService.updateUser(
                id,
                request.getUsername(),
                request.getPassword(),
                request.getRole(),
                request.getTokenBalance(),
                null // keep current enabled status
        );
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> userStats() {
        List<ProxyUser> users = userService.listUsers();
        long totalUsers = users.size();
        long totalTokens = users.stream().mapToLong(ProxyUser::getTokenBalance).sum();
        long totalUsed = users.stream().mapToLong(ProxyUser::getTokenUsed).sum();
        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalTokens", totalTokens,
                "totalUsed", totalUsed,
                "totalRemaining", totalTokens - totalUsed
        ));
    }

    // ==================== Token 分配 ====================

    @PostMapping("/users/{id}/tokens")
    public ResponseEntity<?> allocateTokens(
            @PathVariable Long id,
            @Valid @RequestBody TokenAllocationRequest request) {
        ProxyUser user = userService.allocateTokens(id, null, request.getAmount(), request.getNote());
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/users/{id}/tokens")
    public ResponseEntity<List<TokenRecord>> getUserTokenRecords(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserTokenRecords(id));
    }
}
