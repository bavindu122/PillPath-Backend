package com.leo.pillpathbackend.dto.otcorder;

import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Long customerId;
    private PaymentMethod paymentMethod;
    private String deliveryAddress;
    private String notes;
    private List<OrderItemDTO> items;
}