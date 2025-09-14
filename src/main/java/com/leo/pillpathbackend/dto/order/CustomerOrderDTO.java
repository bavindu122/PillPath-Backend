package com.leo.pillpathbackend.dto.order;

import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CustomerOrderDTO {
    private Long orderId;
    private String orderCode;
    private String createdAt;
    private String updatedAt;
    private String prescriptionCode;
    private CustomerOrderStatus status;
    private PaymentDTO payment;
    private OrderTotalsDTO totals;
    private String currency;
    private List<PharmacyOrderDTO> pharmacyOrders;
}

