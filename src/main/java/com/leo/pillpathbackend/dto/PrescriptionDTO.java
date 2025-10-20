package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.dto.order.OrderTotalsDTO;
import com.leo.pillpathbackend.dto.order.PharmacyOrderDTO;
import com.leo.pillpathbackend.entity.enums.DeliveryPreference;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PrescriptionDTO {
    private Long id;
    private String code;

    private Long customerId;
    private String customerName;

    private Long pharmacyId;
    private String pharmacyName;

    private PrescriptionStatus status;

    private String imageUrl;
    private String note;

    private DeliveryPreference deliveryPreference;
    private String deliveryAddress;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private BigDecimal totalPrice;

    private List<PrescriptionItemDTO> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Order information (if prescription has been ordered)
    private String orderCode;
    private OrderTotalsDTO orderTotals;
    private List<PharmacyOrderDTO> pharmacyOrders;
}

