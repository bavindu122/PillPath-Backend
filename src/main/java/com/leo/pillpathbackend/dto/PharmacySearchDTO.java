package com.leo.pillpathbackend.dto;

import lombok.*;

/**
 * DTO for pharmacy search response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacySearchDTO {

    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String logoUrl;
    private Boolean isVerified;
    private Boolean isActive;
    private Double averageRating;
    private Integer totalReviews;
}

