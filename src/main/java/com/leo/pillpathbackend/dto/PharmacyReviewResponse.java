package com.leo.pillpathbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PharmacyReviewResponse {
    private String id; // review_id UUID
    private Long customerId;
    private Long pharmacyId;
    private Integer rating;
    private String review;
    private String createdAt; // ISO string
}

