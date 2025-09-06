package com.leo.pillpathbackend.dto.orderpreview;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderPreviewItemDTO {
    private Long id;
    private String medicineName;
    private String genericName;
    private String dosage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean available;
    private String notes;
}

