package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum PrescriptionStatus {
    PENDING("Pending Verification"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    FULFILLED("Fulfilled");

    private final String displayName;

    PrescriptionStatus(String displayName) {
        this.displayName = displayName;
    }
}