package com.leo.pillpathbackend.dto.otcorder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDetailDTO {
    private Long itemId;
    private String medicineName;
    private String dosage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}