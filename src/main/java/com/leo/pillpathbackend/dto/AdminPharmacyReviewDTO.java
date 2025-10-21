package com.leo.pillpathbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPharmacyReviewDTO {
    private String id;
    private String customerName;
    private String pharmacyName;
    private String review;
    private Integer rating;
    private String createdAt; // ISO 8601
}

