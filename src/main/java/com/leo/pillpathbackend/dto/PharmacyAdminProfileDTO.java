package com.leo.pillpathbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PharmacyAdminProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String profilePictureUrl;
    private Boolean emailVerified;
    private Boolean phoneVerified;

    // PharmacyAdmin specific fields
    private Long pharmacyId;
    private String pharmacyName;
    private String position;
    private String licenseNumber;
    private LocalDate hireDate;
    private Boolean isPrimaryAdmin;
    private List<String> permissions;
}