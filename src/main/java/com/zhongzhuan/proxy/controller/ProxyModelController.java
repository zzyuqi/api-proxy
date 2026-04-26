package com.zhongzhuan.proxy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Local OpenAI-compatible endpoints for client discovery (e.g. CherryStudio).
 * Handles model listing locally instead of proxying to MiniMax.
 */
@RestController
public class ProxyModelController {

    @GetMapping("/api/proxy/minimax/v1/models")
    public ResponseEntity<?> listModels() {
        List<Map<String, Object>> models = List.of(
                modelObj("MiniMax-M2.7", "minimax"),
                modelObj("MiniMax-M2.7-highspeed", "minimax"),
                modelObj("MiniMax-Text-01", "minimax"),
                modelObj("abab6.5-chat", "minimax"),
                modelObj("abab5.5-chat", "minimax"),
                modelObj("abab5.5s-chat", "minimax")
        );
        return ResponseEntity.ok(Map.of("object", "list", "data", models));
    }

    private static Map<String, Object> modelObj(String id, String ownedBy) {
        return Map.of(
                "id", id,
                "object", "model",
                "created", 1700000000,
                "owned_by", ownedBy
        );
    }
}
