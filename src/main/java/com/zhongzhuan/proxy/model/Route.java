package com.zhongzhuan.proxy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "route")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "path_pattern", nullable = false, length = 255)
    private String pathPattern;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(length = 10)
    private String method;

    @Column(name = "requires_auth")
    private Boolean requiresAuth = false;

    @Column(name = "rate_limit")
    private Integer rateLimit = 0;

    @Column(name = "cache_ttl")
    private Integer cacheTtl = 0;

    @Column(name = "allowed_models", length = 500)
    private String allowedModels;

    @Column(length = 20)
    private String status = "ENABLED";

    @Column(name = "user_visible")
    private Boolean userVisible = true;

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
    public String getPathPattern() { return pathPattern; }
    public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Boolean getRequiresAuth() { return requiresAuth; }
    public void setRequiresAuth(Boolean requiresAuth) { this.requiresAuth = requiresAuth; }
    public Integer getRateLimit() { return rateLimit; }
    public void setRateLimit(Integer rateLimit) { this.rateLimit = rateLimit; }
    public Integer getCacheTtl() { return cacheTtl; }
    public void setCacheTtl(Integer cacheTtl) { this.cacheTtl = cacheTtl; }
    public String getAllowedModels() { return allowedModels; }
    public void setAllowedModels(String allowedModels) { this.allowedModels = allowedModels; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getUserVisible() { return userVisible; }
    public void setUserVisible(Boolean userVisible) { this.userVisible = userVisible; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
