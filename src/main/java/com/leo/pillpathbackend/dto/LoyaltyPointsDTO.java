package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyPointsDTO {
    private Integer currentPoints;
    private BigDecimal pointsRate;
    private String rateDescription;
    private Long customerId;
    private String customerName;
}
