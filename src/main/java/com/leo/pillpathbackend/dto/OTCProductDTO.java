package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTCProductDTO {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private Boolean inStock;
    private String imageUrl;
    private Double rating;
    private String category;
    private String dosage;
    private Integer stockQuantity;
}