package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum UserType {
    CUSTOMER("Customer"),
    ADMIN("System Admin"),
    PHARMACY_ADMIN("Pharmacy Admin"),
    PHARMACIST("Pharmacist");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }
}
