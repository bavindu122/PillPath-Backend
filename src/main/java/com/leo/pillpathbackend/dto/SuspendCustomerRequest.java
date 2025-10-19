package com.leo.pillpathbackend.dto;

import jakarta.validation.constraints.NotBlank;

public class SuspendCustomerRequest {
    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
