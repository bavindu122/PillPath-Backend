package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyDTO {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phoneNumber;
    private String email;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;

    // Cloudinary fields
    private String logoUrl;
    private String logoPublicId;
    private String bannerUrl;
    private String bannerPublicId;

    private Map<String, String> operatingHours;
    private List<String> services;
    private Boolean isVerified;
    private Boolean isActive;
    private Boolean deliveryAvailable;
    private Integer deliveryRadius;
    private Double averageRating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Frontend specific fields
    private String status; // Derived from isActive + isVerified
    private String rejectReason;
    private String suspendReason;

    // Additional fields for profile view
    private Boolean hasDelivery;
    private Boolean has24HourService;
    private Boolean acceptsInsurance;
    private Boolean hasVaccinations;
    private String currentStatus;
    private List<ReviewDTO> recentReviews;
    private List<OTCProductDTO> popularProducts;
}