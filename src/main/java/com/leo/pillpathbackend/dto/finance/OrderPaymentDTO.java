package com.leo.pillpathbackend.dto.finance;

import com.leo.pillpathbackend.entity.enums.SettlementChannel;
import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentDTO {
    private String id;
    private String orderCode;
    private String date; // ISO 8601
    private String customerName;
    private String pharmacyId;
    private String pharmacyName;
    private SettlementChannel settlementChannel; // ONLINE | ON_HAND
    private PaymentMethod paymentMethod;
    private BigDecimal grossAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal netPayoutAmount;
    private Boolean onHandCommissionReceived; // null for ONLINE
    private String commissionId; // nullable
    private String payoutId; // nullable
}

