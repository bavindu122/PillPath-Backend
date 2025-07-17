//package com.leo.pillpathbackend.entity;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.*;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "customers")
//@Data
//@EqualsAndHashCode(callSuper = true)
//@NoArgsConstructor
//@AllArgsConstructor
//@DiscriminatorValue("CUSTOMER")
//@PrimaryKeyJoinColumn(name = "user_id")
//public class Customer extends User {
//
//    @NotBlank(message = "First name is required")
//    @Size(max = 50, message = "First name cannot exceed 50 characters")
//    @Column(name = "first_name", nullable = false, length = 50)
//    private String firstName;
//
//    @NotBlank(message = "Last name is required")
//    @Size(max = 50, message = "Last name cannot exceed 50 characters")
//    @Column(name = "last_name", nullable = false, length = 50)
//    private String lastName;
//
//    @NotNull(message = "Date of birth is required")
//    @Past(message = "Date of birth must be in the past")
//    @Column(name = "date_of_birth", nullable = false)
//    private LocalDate dateOfBirth;
//
//    // Basic profile information
//    @Enumerated(EnumType.STRING)
//    @Column(name = "gender")
//    private Gender gender;
//
//    // Address information (basic)
//    @Column(name = "address", columnDefinition = "TEXT")
//    private String address;
//
//    @Column(name = "city", length = 50)
//    private String city;
//
//    // Customer activity tracking
//    @Column(name = "total_orders", nullable = false)
//    private Integer totalOrders = 0;
//
//    @Column(name = "last_order_date")
//    private LocalDateTime lastOrderDate;
//
//    // Preferences
//    @Column(name = "notification_enabled", nullable = false)
//    private Boolean notificationEnabled = true;
//
//    // Utility methods
//    public String getFullName() {
//        return firstName + " " + lastName;
//    }
//
//    public int getAge() {
//        return LocalDate.now().getYear() - dateOfBirth.getYear();
//    }
//
//    public boolean isAdult() {
//        return getAge() >= 18;
//    }
//
//    // Enums
//    public enum Gender {
//        MALE,
//        FEMALE,
//        OTHER,
//        PREFER_NOT_TO_SAY
//    }
//}