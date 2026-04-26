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

    @Column(name = "token_balance")
    private Long tokenBalance = 0L;

    @Column(name = "token_used")
    private Long tokenUsed = 0L;

    private Boolean enabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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
    public Long getTokenBalance() { return tokenBalance; }
    public void setTokenBalance(Long tokenBalance) { this.tokenBalance = tokenBalance; }
    public Long getTokenUsed() { return tokenUsed; }
    public void setTokenUsed(Long tokenUsed) { this.tokenUsed = tokenUsed; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
