package com.leo.pillpathbackend.dto.wallet;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class WalletSummaryDTO {
    String ownerType;
    Long ownerId;
    String currency;
    BigDecimal balance;
    List<WalletTransactionDTO> transactions;
}

