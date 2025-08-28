package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum PrescriptionStatus {
    // New workflow
    PENDING_REVIEW("Pending Review"),
    IN_PROGRESS("In Progress"),
    CLARIFICATION_NEEDED("Clarification Needed"),
    READY_FOR_PICKUP("Ready for Pickup"),
    REJECTED("Rejected"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),

    // Legacy statuses kept for backward compatibility
    PENDING("Pending"),
    VERIFIED("Verified"),
    EXPIRED("Expired"),
    FULFILLED("Fulfilled");

    private final String displayName;

    PrescriptionStatus(String displayName) {
        this.displayName = displayName;
    }
}