package com.leo.pillpathbackend.dto;

import lombok.Data;

@Data
public class PharmacyRegistrationResponse {
    private Long pharmacyId;
    private Long adminId;
    private String pharmacyName;
    private String adminUsername;
    private String adminEmail;
    private String message;
    private boolean success;
}