package com.zhongzhuan.proxy.dto;

import jakarta.validation.constraints.NotBlank;

public class RouteRequest {

    @NotBlank(message = "路由名称不能为空")
    private String name;

    @NotBlank(message = "路径匹配模式不能为空")
    private String pathPattern;

    @NotBlank(message = "目标地址不能为空")
    private String targetUrl;

    private String method;
    private Boolean requiresAuth = false;
    private Integer rateLimit = 0;
    private Integer cacheTtl = 0;
    private String allowedModels;
    private String status = "ENABLED";
    private Boolean userVisible = true;

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAllowedModels() { return allowedModels; }
    public void setAllowedModels(String allowedModels) { this.allowedModels = allowedModels; }
    public Boolean getUserVisible() { return userVisible; }
    public void setUserVisible(Boolean userVisible) { this.userVisible = userVisible; }
}
