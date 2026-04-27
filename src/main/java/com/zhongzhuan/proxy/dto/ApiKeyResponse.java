package com.zhongzhuan.proxy.dto;

import com.zhongzhuan.proxy.model.ApiKey;

import java.time.LocalDateTime;

public class ApiKeyResponse {

    private Long id;
    private String keyValue;
    private String keyValueMasked;
    private String name;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private Long userId;

    public static ApiKeyResponse from(ApiKey apiKey) {
        ApiKeyResponse resp = new ApiKeyResponse();
        resp.setId(apiKey.getId());
        resp.setKeyValue(apiKey.getKeyValue());
        resp.setKeyValueMasked(maskKey(apiKey.getKeyValue()));
        resp.setName(apiKey.getName());
        resp.setEnabled(apiKey.getEnabled());
        resp.setCreatedAt(apiKey.getCreatedAt());
        resp.setUserId(apiKey.getUserId());
        return resp;
    }

    private static String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "********";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }
    public String getKeyValueMasked() { return keyValueMasked; }
    public void setKeyValueMasked(String keyValueMasked) { this.keyValueMasked = keyValueMasked; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
