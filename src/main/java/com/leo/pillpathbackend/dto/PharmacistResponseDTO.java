package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.Pharmacist;
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

    public PharmacistResponseDTO(Pharmacist pharmacist) {
        this.id = pharmacist.getId();
        this.fullName = pharmacist.getFullName();
        this.email = pharmacist.getEmail();
        this.phoneNumber = pharmacist.getPhoneNumber();
        this.dateOfBirth = pharmacist.getDateOfBirth();
        this.profilePictureUrl = pharmacist.getProfilePictureUrl();
        this.licenseNumber = pharmacist.getLicenseNumber();
        this.licenseExpiryDate = pharmacist.getLicenseExpiryDate();
        this.specialization = pharmacist.getSpecialization();
        this.yearsOfExperience = pharmacist.getYearsOfExperience();
        this.hireDate = pharmacist.getHireDate();
        this.shiftSchedule = pharmacist.getShiftSchedule();
        this.certifications = pharmacist.getCertifications();
        this.isActive = pharmacist.getIsActive();
        this.isVerified = pharmacist.getIsVerified();
        this.employmentStatus = pharmacist.getEmploymentStatus();
        this.createdAt = pharmacist.getCreatedAt();
        this.updatedAt = pharmacist.getUpdatedAt();
    }
}