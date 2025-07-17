package com.leo.pillpathbackend.entity.enums;

import lombok.Getter;

@Getter
public enum AdminLevel {
    STANDARD("Standard Admin"),
    SUPER("Super Admin"),
    SYSTEM("System Admin");

    private final String displayName;

    AdminLevel(String displayName) {
        this.displayName = displayName;
    }
}
