package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyPerformanceResponseDTO {
    private String pharmacyId;
    private String name;
    private long ordersFulfilled;
    private double rating;
    private String status; // Active | Suspended | Pending
    private String registrationDate; // ISO-8601 string
}

