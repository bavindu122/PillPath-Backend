package com.leo.pillpathbackend.dto.order;

import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import com.leo.pillpathbackend.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentDTO {
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String reference;
}

