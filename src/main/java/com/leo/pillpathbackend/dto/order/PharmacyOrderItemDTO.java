package com.leo.pillpathbackend.dto.order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PharmacyOrderItemDTO {
    private Long itemId;
    private Long previewItemId; // submission item id
    private String medicineName;
    private String genericName;
    private String dosage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String notes;
}

