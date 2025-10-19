package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewChartsResponseDTO {
    private List<UserRegistrationTrendItem> userRegistrationTrend;
    private List<RoleDistributionItem> userRolesDistribution;
    private List<PharmacyOnboardingItem> pharmacyOnboardingData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegistrationTrendItem {
        private String month;   // e.g., "May"
        private String isoMonth; // e.g., "2025-05"
        private int users;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDistributionItem {
        private String name;  // Customers | Pharmacists | Pharmacy Admins | System Admins
        private int value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PharmacyOnboardingItem {
        private String month;   // e.g., "May"
        private String isoMonth; // e.g., "2025-05"
        private int pharmacies;
    }
}

