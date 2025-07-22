package com.leo.pillpathbackend.dto;

import lombok.Data;

@Data
public class PharmacyAdminLoginResponse {
    private Long adminId;
    private Long pharmacyId;
    private String username;
    private String email;
    private String fullName;
    private boolean success;
    private String message;
    private String token;
    private PharmacyAdminProfileDTO user;
}