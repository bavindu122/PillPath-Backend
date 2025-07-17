package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    PARTIAL("Partially Paid");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}