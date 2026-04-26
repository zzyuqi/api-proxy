package com.zhongzhuan.proxy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_log")
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "api_key_id")
    private Long apiKeyId;

    @Column(name = "request_path", length = 500)
    private String requestPath;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public Long getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(Long apiKeyId) { this.apiKeyId = apiKeyId; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
