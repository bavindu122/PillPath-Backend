package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyRegistrationRequest {
    // Pharmacy details
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private Map<String, String> operatingHours;
    private List<String> services;
    private Boolean deliveryAvailable;
    private Integer deliveryRadius;

    // Add location fields
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Admin details
    private String adminFirstName;
    private String adminLastName;
    private String adminEmail;
    private String adminPassword;
    private String adminPhoneNumber;
    private String adminPosition;
    private String adminLicenseNumber;
}