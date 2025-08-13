package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacistResponseDTO {
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
    private EmploymentStatus employmentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
