package com.leo.pillpathbackend.dto.otcorder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private String orderCode;
    private String status;
    private BigDecimal total;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private List<PharmacyOrderDTO> pharmacyOrders;
}