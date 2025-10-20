package com.leo.pillpathbackend.dto.wallet;

import com.leo.pillpathbackend.entity.enums.WalletTransactionType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class WalletTransactionDTO {
    Long id;
    WalletTransactionType type;
    BigDecimal amount;
    BigDecimal balanceAfter;
    String currency;
    String orderCode;
    Long pharmacyOrderId;
    Long prescriptionId;
    String paymentId;
    String externalKey;
    String note;
    LocalDateTime createdAt;
}

