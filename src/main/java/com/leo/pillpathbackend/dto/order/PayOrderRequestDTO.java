package com.leo.pillpathbackend.dto.order;

import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import lombok.Data;

@Data
public class PayOrderRequestDTO {
    private PaymentMethod paymentMethod; // optional override at payment
    private String reference; // external payment reference / transaction id (optional)
}

