package com.leo.pillpathbackend.dto.activity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PharmacyActivityDTO {
    private String pharmacyId;
    private String pharmacyName;
    private String address;
    private ActivityStatus status;
    private Actions actions;
    private List<MedicationSummaryDTO> medications; // nullable
    private TotalsDTO totals; // nullable

    @Data
    @Builder
    public static class Actions {
        private boolean canViewOrderPreview;
        private boolean canProceedToPayment;
    }
}

