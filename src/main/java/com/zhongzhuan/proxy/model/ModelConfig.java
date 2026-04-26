package com.zhongzhuan.proxy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_config")
public class ModelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "api_url", nullable = false, length = 500)
    private String apiUrl;

    @Column(name = "api_key", nullable = false, length = 255)
    private String apiKey;

    @Column(name = "default_model_id", length = 100)
    private String defaultModelId;

    @Column(length = 500)
    private String models;

    @Column(length = 20)
    private String status = "ENABLED";

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}