package com.zhongzhuan.proxy.dto;

import com.zhongzhuan.proxy.model.Route;

import java.time.LocalDateTime;

public class RouteResponse {

    private Long id;
    private String name;
    private String pathPattern;
    private String targetUrl;
    private String method;
    private Boolean requiresAuth;
    private Integer rateLimit;
    private Integer cacheTtl;
    private String allowedModels;
    private String status;
    private Boolean userVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RouteResponse from(Route route) {
        RouteResponse resp = new RouteResponse();
        resp.setId(route.getId());
        resp.setName(route.getName());
        resp.setPathPattern(route.getPathPattern());
        resp.setTargetUrl(route.getTargetUrl());
        resp.setMethod(route.getMethod());
        resp.setRequiresAuth(route.getRequiresAuth());
        resp.setRateLimit(route.getRateLimit());
        resp.setCacheTtl(route.getCacheTtl());
        resp.setAllowedModels(route.getAllowedModels());
        resp.setStatus(route.getStatus());
        resp.setUserVisible(route.getUserVisible());
        resp.setCreatedAt(route.getCreatedAt());
        resp.setUpdatedAt(route.getUpdatedAt());
        return resp;
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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
