package com.leo.pillpathbackend.dto.activity;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MedicationSummaryDTO {
    private String medicationId; // prescription item id
    private String name; // medicineName
    private String strength; // mapped from dosage or genericName if needed
    private Integer quantity;
    private BigDecimal price; // total price for line OR unitPrice
    private Boolean available;
    private String notes;
}

