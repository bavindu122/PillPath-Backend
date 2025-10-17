package com.leo.pillpathbackend.enums;

public enum NotificationType {
    SUCCESS,    // Green - Positive actions (order ready, confirmed)
    INFO,       // Blue - General information (prescription sent, preview ready)
    WARNING,    // Yellow - Warnings (low stock, expiring)
    ERROR       // Red - Errors (order declined, cancelled)
}
