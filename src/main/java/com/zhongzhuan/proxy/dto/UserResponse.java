package com.zhongzhuan.proxy.dto;

import com.zhongzhuan.proxy.model.ProxyUser;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String role;
    private String userToken;
    private Long requestCount;
    private Long requestsUsed;
    private Long requestsRemaining;
    private Integer concurrentLimit;
    private Integer hourlyLimit;
    private Integer currentConcurrent;
    private Integer currentHourly;
    private String password;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;

    public static UserResponse from(ProxyUser user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setPassword(user.getPassword());
        resp.setRole(user.getRole());
        resp.setUserToken(user.getUserToken());
        resp.setRequestCount(user.getRequestCount());
        resp.setRequestsUsed(user.getRequestsUsed());
        resp.setRequestsRemaining(user.getRequestCount() - user.getRequestsUsed());
        resp.setConcurrentLimit(user.getConcurrentLimit());
        resp.setHourlyLimit(user.getHourlyLimit());
        resp.setEnabled(user.getEnabled());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setExpireAt(user.getExpireAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUserToken() { return userToken; }
    public void setUserToken(String userToken) { this.userToken = userToken; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Long getRequestCount() { return requestCount; }
    public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
    public Long getRequestsUsed() { return requestsUsed; }
    public void setRequestsUsed(Long requestsUsed) { this.requestsUsed = requestsUsed; }
    public Long getRequestsRemaining() { return requestsRemaining; }
    public void setRequestsRemaining(Long requestsRemaining) { this.requestsRemaining = requestsRemaining; }
    public Integer getConcurrentLimit() { return concurrentLimit; }
    public void setConcurrentLimit(Integer concurrentLimit) { this.concurrentLimit = concurrentLimit; }
    public Integer getHourlyLimit() { return hourlyLimit; }
    public void setHourlyLimit(Integer hourlyLimit) { this.hourlyLimit = hourlyLimit; }
    public Integer getCurrentConcurrent() { return currentConcurrent; }
    public void setCurrentConcurrent(Integer currentConcurrent) { this.currentConcurrent = currentConcurrent; }
    public Integer getCurrentHourly() { return currentHourly; }
    public void setCurrentHourly(Integer currentHourly) { this.currentHourly = currentHourly; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
