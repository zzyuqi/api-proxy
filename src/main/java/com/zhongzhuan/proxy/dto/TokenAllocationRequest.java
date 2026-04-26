package com.zhongzhuan.proxy.dto;

import jakarta.validation.constraints.NotNull;

public class TokenAllocationRequest {

    @NotNull(message = "数量不能为空")
    private Long amount;

    private String note;

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
