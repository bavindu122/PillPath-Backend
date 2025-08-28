package com.leo.pillpathbackend.entity;
import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import com.leo.pillpathbackend.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.leo.pillpathbackend.entity.enums.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pharmacists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pharmacist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     //Remove these duplicate fields - they exist in PharmacistUser
     @Column(unique = true, nullable = false)
     private String email;

     @Column(nullable = false)
     private String password;

     @Column(name = "full_name", nullable = false)
     private String fullName;

     @Column(name = "phone_number")
     private String phoneNumber;

     @Column(name = "date_of_birth")
     private LocalDate dateOfBirth;

     @Column(name = "profile_picture_url")
     private String profilePictureUrl;

     //Pharmacist-specific fields only
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "shift_schedule")
    private String shiftSchedule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "certifications", columnDefinition = "jsonb")
    private List<String> certifications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "verifiedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prescription> verifiedPrescriptions = new ArrayList<>();

    @OneToMany(mappedBy = "processedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> processedOrders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pharmacist_user_id", nullable = false)
    private PharmacistUser pharmacistUser;

    public UserType getUserType() {
        return UserType.PHARMACIST;
    }

    public PharmacistUser getUser() {
        return pharmacistUser;
    }

    public void setUser(PharmacistUser pharmacistUser) {
        this.pharmacistUser = pharmacistUser;
    }

    // Convenience methods to access user fields
    public String getEmail() {
        return pharmacistUser != null ? pharmacistUser.getEmail() : null;
    }

    public String getFullName() {
        return pharmacistUser != null ? pharmacistUser.getFullName() : null;
    }

    public String getPhoneNumber() {
        return pharmacistUser != null ? pharmacistUser.getPhoneNumber() : null;
    }
}