package com.leo.pillpathbackend.dto.wallet.events;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PharmacyCommissionCardCapturedEvent {
    private Long pharmacyId;
    private String orderCode;
    private BigDecimal commissionAmount;
    private String paymentId;
    private String externalKey;
}

