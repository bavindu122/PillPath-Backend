package com.leo.pillpathbackend.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PrescriptionItemDTO {
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

