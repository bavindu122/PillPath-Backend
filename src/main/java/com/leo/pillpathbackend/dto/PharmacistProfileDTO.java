package com.leo.pillpathbackend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PharmacistProfileDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;

    // Pharmacist specific fields
    private Long pharmacyId;
    private String pharmacyName;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String specialization;
    private Integer yearsOfExperience;
    private LocalDate hireDate;
    private String shiftSchedule;
    private List<String> certifications;
    private Boolean isVerified;
    private Boolean isActive;
}