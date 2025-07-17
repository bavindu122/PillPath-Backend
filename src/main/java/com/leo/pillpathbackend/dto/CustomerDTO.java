package com.leo.pillpathbackend.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String profilePictureUrl;
    private Boolean isActive;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String userType;

    // Customer specific fields
    private String insuranceProvider;
    private String insuranceId;
    private List<String> allergies = new ArrayList<>();
    private List<String> medicalConditions = new ArrayList<>();
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Long preferredPharmacyId;
}