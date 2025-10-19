package com.leo.pillpathbackend.dto.finance;

import com.leo.pillpathbackend.entity.enums.PayoutStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutDTO {
    private String id;
    private String orderId; // optional
    private String orderCode; // optional
    private String pharmacyId;
    private String pharmacyName;
    private BigDecimal amount;
    private String month; // MM/YYYY
    private PayoutStatus status;
    private String paidAt; // ISO
    private String receiptUrl;
    private String receiptFileName;
    private String receiptFileType;
    private String note;
    private String createdAt;
    private String updatedAt;
}

