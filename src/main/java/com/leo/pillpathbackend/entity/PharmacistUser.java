// src/main/java/com/leo/pillpathbackend/entity/PharmacistUser.java
package com.leo.pillpathbackend.entity;

import java.time.LocalDate;

import com.leo.pillpathbackend.entity.enums.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("PHARMACIST")
@Getter
@Setter
@NoArgsConstructor
public class PharmacistUser extends User {

    @Override
    public UserType getUserType() {
        return UserType.PHARMACIST;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;
    
    @Column(name = "license_number")
    private String licenseNumber;
    
    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;
    
    @Column(name = "specialization")
    private String specialization;
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "shift_schedule")
    private String shiftSchedule;
    
    @Column(name = "certifications")
    private String certifications;
    
    @Column(name = "is_verified")
    private Boolean isVerified;

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public String getLicenseNumber() {
    return licenseNumber;
}
public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
}

public LocalDate getLicenseExpiryDate() {
    return licenseExpiryDate;
}
public void setLicenseExpiryDate(LocalDate licenseExpiryDate) {
    this.licenseExpiryDate = licenseExpiryDate;
}

public String getSpecialization() {
    return specialization;
}
public void setSpecialization(String specialization) {
    this.specialization = specialization;
}

public Integer getYearsOfExperience() {
    return yearsOfExperience;
}
public void setYearsOfExperience(Integer yearsOfExperience) {
    this.yearsOfExperience = yearsOfExperience;
}

public LocalDate getHireDate() {
    return hireDate;
}
public void setHireDate(LocalDate hireDate) {
    this.hireDate = hireDate;
}

public String getShiftSchedule() {
    return shiftSchedule;
}
public void setShiftSchedule(String shiftSchedule) {
    this.shiftSchedule = shiftSchedule;
}

public String getCertifications() {
    return certifications;
}
public void setCertifications(String certifications) {
    this.certifications = certifications;
}

public Boolean getIsVerified() {
    return isVerified;
}
public void setIsVerified(Boolean isVerified) {
    this.isVerified = isVerified;
}
}