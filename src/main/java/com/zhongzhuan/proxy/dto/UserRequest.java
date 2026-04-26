package com.zhongzhuan.proxy.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String role = "USER";
    private Long tokenBalance = 0L;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getTokenBalance() { return tokenBalance; }
    public void setTokenBalance(Long tokenBalance) { this.tokenBalance = tokenBalance; }
}
