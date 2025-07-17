package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum EmploymentStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    TERMINATED("Terminated");

    private final String displayName;

    EmploymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
