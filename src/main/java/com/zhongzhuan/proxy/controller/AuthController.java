package com.zhongzhuan.proxy.controller;

import com.zhongzhuan.proxy.dto.UserResponse;
import com.zhongzhuan.proxy.model.ProxyUser;
import com.zhongzhuan.proxy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名不能为空"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "密码不能为空"));
        }
        if (password.length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "密码长度不能少于3位"));
        }

        // Check duplicate
        if (userService.getUserByUsername(username) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
        }

        ProxyUser user = userService.createUser(username, password, "USER", 0L);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole(),
                "userToken", user.getUserToken()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
        }

        return userService.authenticate(username, password)
                .map(user -> ResponseEntity.ok((Object) Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "userToken", user.getUserToken(),
                        "tokenBalance", user.getTokenBalance(),
                        "tokenUsed", user.getTokenUsed(),
                        "tokenRemaining", user.getTokenBalance() - user.getTokenUsed()
                )))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误")));
    }
}
