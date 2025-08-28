package com.leo.pillpathbackend.dto.activity;

/**
 * Activity status exposed to frontend. For now mirror PrescriptionStatus values.
 * Can be decoupled later if workflow diverges.
 */
public enum ActivityStatus {
    PENDING_REVIEW,
    IN_PROGRESS,
    CLARIFICATION_NEEDED,
    READY_FOR_PICKUP,
    REJECTED,
    COMPLETED,
    CANCELLED,
    PENDING,
    VERIFIED,
    EXPIRED,
    FULFILLED
}

