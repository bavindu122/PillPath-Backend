package com.leo.pillpathbackend.dto.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletSettingsDTO {
    private String currency;
    private BigDecimal commissionPercent;
    private BigDecimal convenienceFee;
    private Long version;
}

