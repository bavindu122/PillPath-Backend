package com.leo.pillpathbackend.dto.otcorder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyOrderDTO {
    private Long pharmacyOrderId;
    private Long pharmacyId;
    private String pharmacyName;
    private BigDecimal subtotal;
    private String status;
    private List<OrderItemDetailDTO> items;
}