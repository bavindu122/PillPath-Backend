package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfileDTO {
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
    private String insuranceProvider;
    private String insuranceId;
    private List<String> allergies;
    private List<String> medicalConditions;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Long preferredPharmacyId;
    // Excludes: password, isActive, userType, internal flags
}