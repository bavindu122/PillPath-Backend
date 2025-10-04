package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.PharmacistUser;
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

    public static PharmacistResponseDTO fromEntity(PharmacistUser u) {
        PharmacistResponseDTO dto = new PharmacistResponseDTO();
        dto.id = u.getId();
        dto.fullName = u.getFullName();
        dto.email = u.getEmail();
        dto.phoneNumber = u.getPhoneNumber();
        dto.dateOfBirth = u.getDateOfBirth();
        dto.profilePictureUrl = u.getProfilePictureUrl();
        if (u.getPharmacy() != null) {
            dto.pharmacyId = u.getPharmacy().getId();
            dto.pharmacyName = u.getPharmacy().getName();
        }
        dto.licenseNumber = u.getLicenseNumber();
        dto.licenseExpiryDate = u.getLicenseExpiryDate();
        dto.specialization = u.getSpecialization();
        dto.yearsOfExperience = u.getYearsOfExperience();
        dto.hireDate = u.getHireDate();
        dto.isActive = u.getIsActive();
        dto.isVerified = u.getIsVerified();
        dto.employmentStatus = u.getEmploymentStatus();
        dto.shiftSchedule = u.getShiftSchedule();
        dto.certifications = u.getCertifications() != null ? u.getCertifications() : new ArrayList<>();
        dto.createdAt = u.getCreatedAt();
        dto.updatedAt = u.getUpdatedAt();
        return dto;
    }
}