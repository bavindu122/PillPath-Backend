package com.leo.pillpathbackend.dto.order;

import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
public class PharmacyOrderDTO {
    private Long pharmacyOrderId;
    private Long pharmacyId;
    private String pharmacyName;
    private PharmacyOrderStatus status;
    private String pickupCode;
    private String pickupLocation;
    private Double pickupLat;
    private Double pickupLng;
    private String customerNote;
    private String pharmacistNote;
    private String createdAt;
    private String updatedAt;
    private String completedDate;
    private String orderCode;
    private PaymentDTO payment;
    private String customerName;
    private String patientEmail;
    private String patientPhone;
    private String patientAddress;
    private Long prescriptionId;
    private String prescriptionCode;
    private String prescriptionImageUrl;
    private List<PharmacyOrderItemDTO> items;
    private OrderTotalsDTO totals;
}
