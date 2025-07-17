package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum OrderType {
    PRESCRIPTION("Prescription Order"),
    OVER_THE_COUNTER("Over-the-Counter"),
    REFILL("Prescription Refill");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }
}