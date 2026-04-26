package com.zhongzhuan.proxy.dto;

import com.zhongzhuan.proxy.model.ModelConfig;
import java.time.LocalDateTime;

public class ModelConfigResponse {

    private Long id;
    private String name;
    private String provider;
    private String apiUrl;
    private String apiKeyMasked;
    private String defaultModelId;
    private String models;
    private String status;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getApiKeyMasked() { return apiKeyMasked; }
    public void setApiKeyMasked(String apiKeyMasked) { this.apiKeyMasked = apiKeyMasked; }
    public String getDefaultModelId() { return defaultModelId; }
    public void setDefaultModelId(String defaultModelId) { this.defaultModelId = defaultModelId; }
    public String getModels() { return models; }
    public void setModels(String models) { this.models = models; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ModelConfigResponse from(ModelConfig entity) {
        ModelConfigResponse resp = new ModelConfigResponse();
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setProvider(entity.getProvider());
        resp.setApiUrl(entity.getApiUrl());
        resp.setApiKeyMasked(maskApiKey(entity.getApiKey()));
        resp.setDefaultModelId(entity.getDefaultModelId());
        resp.setModels(entity.getModels());
        resp.setStatus(entity.getStatus());
        resp.setDisplayOrder(entity.getDisplayOrder());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "********";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}