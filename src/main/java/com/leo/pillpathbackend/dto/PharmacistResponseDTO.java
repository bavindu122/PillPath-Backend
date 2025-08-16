package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacistResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
    private Long pharmacyId;
    private String pharmacyName;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String specialization;
    private Integer yearsOfExperience;
    private LocalDate hireDate;
    private Boolean isActive;
    private Boolean isVerified;
    private EmploymentStatus employmentStatus;
    private String shiftSchedule;
    private List<String> certifications = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}