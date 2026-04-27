package com.zhongzhuan.proxy.dto;

import jakarta.validation.constraints.NotBlank;

public class ApiKeyRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    private Boolean enabled;

    private Long userId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
