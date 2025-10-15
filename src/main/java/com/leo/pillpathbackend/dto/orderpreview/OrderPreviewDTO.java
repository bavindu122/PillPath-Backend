package com.leo.pillpathbackend.dto.orderpreview;

import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderPreviewDTO {
    private String code;
    private Long pharmacyId;
    private String pharmacyName;

    private String uploadedAt; // ISO second precision
    private String imageUrl;
    private PrescriptionStatus status;

    private List<OrderPreviewItemDTO> items;
    private OrderPreviewTotalsDTO totals;
    private OrderPreviewActionsDTO actions;
    private List<String> unavailableItems;
}

