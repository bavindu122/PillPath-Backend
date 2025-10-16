package com.leo.pillpathbackend.dto.orderpreview;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderPreviewTotalsDTO {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shipping;
    private BigDecimal total;
    private String currency; // e.g., "LKR"
}

