package com.zhongzhuan.proxy.dto;

import com.zhongzhuan.proxy.model.ProxyUser;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String role;
    private String userToken;
    private Long tokenBalance;
    private Long tokenUsed;
    private Long tokenRemaining;
    private String password;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public static UserResponse from(ProxyUser user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setPassword(user.getPassword());
        resp.setRole(user.getRole());
        resp.setUserToken(user.getUserToken());
        resp.setTokenBalance(user.getTokenBalance());
        resp.setTokenUsed(user.getTokenUsed());
        resp.setTokenRemaining(user.getTokenBalance() - user.getTokenUsed());
        resp.setEnabled(user.getEnabled());
        resp.setCreatedAt(user.getCreatedAt());
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
    public Long getTokenBalance() { return tokenBalance; }
    public void setTokenBalance(Long tokenBalance) { this.tokenBalance = tokenBalance; }
    public Long getTokenUsed() { return tokenUsed; }
    public void setTokenUsed(Long tokenUsed) { this.tokenUsed = tokenUsed; }
    public Long getTokenRemaining() { return tokenRemaining; }
    public void setTokenRemaining(Long tokenRemaining) { this.tokenRemaining = tokenRemaining; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
