package com.zhongzhuan.proxy.controller;

import com.zhongzhuan.proxy.model.ProxyUser;
import com.zhongzhuan.proxy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_SECONDS = 300;

    private static class LoginAttempt {
        int count;
        long lastFailedTime;
        LoginAttempt(int count, long lastFailedTime) {
            this.count = count;
            this.lastFailedTime = lastFailedTime;
        }
    }

    private final ConcurrentHashMap<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }

        LoginAttempt attempt = loginAttempts.get(username);
        long now = Instant.now().getEpochSecond();
        if (attempt != null && attempt.count >= MAX_ATTEMPTS) {
            long elapsed = now - attempt.lastFailedTime;
            if (elapsed < BLOCK_DURATION_SECONDS) {
                long remaining = BLOCK_DURATION_SECONDS - elapsed;
                return ResponseEntity.status(429).body(Map.of(
                        "error", "登录失败次数过多，请 " + remaining + " 秒后重试"
                ));
            }
            loginAttempts.remove(username);
        }

        var optUser = userService.authenticate(username, password);
        if (optUser.isPresent()) {
            loginAttempts.remove(username);
            ProxyUser user = optUser.get();
            userService.updateLastActive(user.getId());
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("id", user.getId());
            resp.put("username", user.getUsername());
            resp.put("role", user.getRole());
            resp.put("userToken", user.getUserToken());
            resp.put("requestCount", user.getRequestCount());
            resp.put("requestsUsed", user.getRequestsUsed());
            resp.put("requestsRemaining", user.getRequestCount() - user.getRequestsUsed());
            if (user.getExpireAt() != null) {
                resp.put("expireAt", user.getExpireAt().toString());
            }
            return ResponseEntity.ok(resp);
        }

        LoginAttempt current = loginAttempts.get(username);
        if (current == null) {
            current = new LoginAttempt(1, now);
        } else {
            current.count++;
            current.lastFailedTime = now;
        }
        loginAttempts.put(username, current);
        int remaining = MAX_ATTEMPTS - current.count;
        if (remaining > 0) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "用户名或密码错误，剩余 " + remaining + " 次尝试机会"
            ));
        }
        return ResponseEntity.status(429).body(Map.of(
                "error", "登录失败次数过多，请 " + BLOCK_DURATION_SECONDS + " 秒后重试"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("X-User-Token") String token) {
        return userService.authenticateByToken(token)
                .map(user -> {
                    userService.updateLastActive(user.getId());
                    Map<String, Object> resp = new java.util.HashMap<>();
                    resp.put("id", user.getId());
                    resp.put("username", user.getUsername());
                    resp.put("role", user.getRole());
                    resp.put("userToken", user.getUserToken());
                    resp.put("requestCount", user.getRequestCount());
                    resp.put("requestsUsed", user.getRequestsUsed());
                    resp.put("requestsRemaining", user.getRequestCount() - user.getRequestsUsed());
                    if (user.getExpireAt() != null) {
                        resp.put("expireAt", user.getExpireAt().toString());
                    }
                    return ResponseEntity.ok((Object) resp);
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "无效的 Token")));
    }
}
