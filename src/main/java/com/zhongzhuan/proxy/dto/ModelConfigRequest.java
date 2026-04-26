package com.zhongzhuan.proxy.dto;

import com.zhongzhuan.proxy.model.ModelConfig;
import jakarta.validation.constraints.NotBlank;

public class ModelConfigRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "提供商不能为空")
    private String provider;

    @NotBlank(message = "API URL不能为空")
    private String apiUrl;

    @NotBlank(message = "API Key不能为空")
    private String apiKey;

    private String defaultModelId;

    private String models;

    private String status = "ENABLED";

    private Integer displayOrder = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getDefaultModelId() { return defaultModelId; }
    public void setDefaultModelId(String defaultModelId) { this.defaultModelId = defaultModelId; }
    public String getModels() { return models; }
    public void setModels(String models) { this.models = models; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public static ModelConfigRequest from(ModelConfig entity) {
        ModelConfigRequest req = new ModelConfigRequest();
        req.setName(entity.getName());
        req.setProvider(entity.getProvider());
        req.setApiUrl(entity.getApiUrl());
        req.setApiKey(entity.getApiKey());
        req.setDefaultModelId(entity.getDefaultModelId());
        req.setModels(entity.getModels());
        req.setStatus(entity.getStatus());
        req.setDisplayOrder(entity.getDisplayOrder());
        return req;
    }
}