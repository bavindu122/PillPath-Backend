package com.leo.pillpathbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        if (operatingHours == null || operatingHours.isEmpty()) {
            return "Operating hours unavailable";
        }
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String dayName = today.toString().substring(0,1) + today.toString().substring(1).toLowerCase(); // e.g., "Monday"
        // Try both "Monday" and "MONDAY" keys
        String hours = operatingHours.get(dayName);
        if (hours == null) {
            hours = operatingHours.get(today.toString()); // fallback to "MONDAY"
        }
        if (hours == null || hours.trim().isEmpty() || hours.equalsIgnoreCase("Closed")) {
            return "Closed";
        }
        if (hours.contains("24") || hours.contains("00:00-23:59")) {
            return "Open 24 hours";
        }
        // Expected format: "HH:mm-HH:mm"
        String[] parts = hours.split("-");
        if (parts.length != 2) {
            return "Operating hours unavailable";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime openTime = LocalTime.parse(parts[0].trim(), formatter);
            LocalTime closeTime = LocalTime.parse(parts[1].trim(), formatter);
            LocalTime now = LocalTime.now();
            if (now.isBefore(openTime)) {
                return "Closed (opens at " + openTime + ")";
            } else if (now.isAfter(closeTime)) {
                return "Closed";
            } else {
                return "Open until " + closeTime;
            }
        } catch (DateTimeParseException e) {
            return "Operating hours unavailable";
        }
    }

    // Helper method to check 24-hour service
    public Boolean getHas24HourService() {
        if (operatingHours == null) return false;
        return operatingHours.values().stream()
                .anyMatch(hours -> hours.contains("24") || hours.contains("00:00-23:59"));
    }
}