package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtcStockAlertDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String status;
    private String imageUrl;
    private String category;
    private String dosage;
    private String manufacturer;
    private String packSize;
    private Long pharmacyId;
    private String pharmacyName;
    private LocalDateTime updatedAt;
}