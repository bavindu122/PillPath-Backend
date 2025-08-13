package com.leo.pillpathbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PharmacyMapDTO {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double averageRating;
    private String phoneNumber;
    private Boolean deliveryAvailable;
    private String logoUrl;
    private Map<String, String> operatingHours;
    private Boolean isActive;
    private Boolean isVerified;

    // Helper method to check if pharmacy is currently open
    public String getCurrentStatus() {
        // Add logic to determine if pharmacy is open based on operatingHours
        return "Open until 10:00 PM"; // Placeholder
    }

    // Helper method to check 24-hour service
    public Boolean getHas24HourService() {
        if (operatingHours == null) return false;
        return operatingHours.values().stream()
                .anyMatch(hours -> hours.contains("24") || hours.contains("00:00-23:59"));
    }
}