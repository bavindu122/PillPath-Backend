package com.leo.pillpathbackend.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UnifiedOrderSummaryDTO {
    private String orderType; // PRESCRIPTION | OTC
    private String orderCode;
    private String createdAt; // ISO string
    private String status;

    private OrderTotalsDTO totals; // For OTC, only 'total' and 'currency' may be filled
    private PaymentDTO payment;    // May be partially filled for OTC depending on data

    // Prescription-specific (list of slices)
    private List<UnifiedPharmacyOrderSummaryDTO> pharmacyOrders;

    // OTC-specific convenience
    private Long pharmacyId;
    private String pharmacyName;

    // Optional note fields
    private String customerNote;
    private String notes;

    // OTC items (optional via includeItems=true)
    private List<UnifiedOtcItemDTO> items;

    @Data
    @Builder
    public static class UnifiedPharmacyOrderSummaryDTO {
        private String orderCode;
        private Long pharmacyId;
        private String pharmacyName;
        private String status;
        private String prescriptionImageUrl;
    }

    @Data
    @Builder
    public static class UnifiedOtcItemDTO {
        private Long otcProductId;        // currently unknown (schema doesn’t store it)
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String productImageUrl;   // currently unknown
    }
}

