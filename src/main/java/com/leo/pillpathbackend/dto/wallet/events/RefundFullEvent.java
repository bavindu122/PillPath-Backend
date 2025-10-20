package com.leo.pillpathbackend.dto.wallet.events;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundFullEvent {
    private String orderCode;
    private Long pharmacyOrderId;
    private Long pharmacyId;
    private BigDecimal amount;
    private String externalKey;
}

