package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionDTO {
    private String orderCode;
    private LocalDateTime orderDate;
    private BigDecimal orderTotal;
    private String paymentMethod;
    private Integer pointsEarned;
    private String orderStatus;
    private BigDecimal loyaltyRate; // Rate at the time points were earned
}
