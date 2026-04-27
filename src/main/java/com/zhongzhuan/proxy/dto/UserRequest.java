package com.zhongzhuan.proxy.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String role = "USER";
    private Long requestCount = 0L;
    private Integer concurrentLimit = 0;
    private Integer hourlyLimit = 0;
    private Boolean enabled;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getRequestCount() { return requestCount; }
    public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
    public Integer getConcurrentLimit() { return concurrentLimit; }
    public void setConcurrentLimit(Integer concurrentLimit) { this.concurrentLimit = concurrentLimit; }
    public Integer getHourlyLimit() { return hourlyLimit; }
    public void setHourlyLimit(Integer hourlyLimit) { this.hourlyLimit = hourlyLimit; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
