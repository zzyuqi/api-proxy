package com.zhongzhuan.proxy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proxy_user")
public class ProxyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String role = "USER";

    @Column(name = "user_token", unique = true, length = 64)
    private String userToken;

    @Column(name = "request_count")
    private Long requestCount = 0L;

    @Column(name = "requests_used")
    private Long requestsUsed = 0L;

    // 并发限制：同一用户最大并发数，0表示不限制
    @Column(name = "concurrent_limit")
    private Integer concurrentLimit = 0;

    // 每小时限制：每小时最大请求次数，0表示不限制
    @Column(name = "hourly_limit")
    private Integer hourlyLimit = 0;

    private Boolean enabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUserToken() { return userToken; }
    public void setUserToken(String userToken) { this.userToken = userToken; }
    public Long getRequestCount() { return requestCount; }
    public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
    public Long getRequestsUsed() { return requestsUsed; }
    public void setRequestsUsed(Long requestsUsed) { this.requestsUsed = requestsUsed; }
    public Integer getConcurrentLimit() { return concurrentLimit; }
    public void setConcurrentLimit(Integer concurrentLimit) { this.concurrentLimit = concurrentLimit; }
    public Integer getHourlyLimit() { return hourlyLimit; }
    public void setHourlyLimit(Integer hourlyLimit) { this.hourlyLimit = hourlyLimit; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
