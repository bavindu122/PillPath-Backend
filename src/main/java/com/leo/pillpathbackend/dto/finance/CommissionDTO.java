package com.leo.pillpathbackend.dto.finance;

import com.leo.pillpathbackend.entity.enums.CommissionStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionDTO {
    private String id;
    private String orderId;
    private String orderCode;
    private String pharmacyId;
    private String pharmacyName;
    private BigDecimal amount;
    private String month; // MM/YYYY
    private CommissionStatus status;
    private String paidAt; // ISO
    private String note;
    private String createdAt;
    private String updatedAt;
}

