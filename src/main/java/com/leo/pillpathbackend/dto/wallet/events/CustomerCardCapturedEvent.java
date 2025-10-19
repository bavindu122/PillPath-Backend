package com.leo.pillpathbackend.dto.wallet.events;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerCardCapturedEvent {
    private String orderCode;
    private Long pharmacyOrderId;
    private Long prescriptionId;
    private Long pharmacyId;
    private BigDecimal amount;
    private String paymentId;
    private String externalKey;
}

