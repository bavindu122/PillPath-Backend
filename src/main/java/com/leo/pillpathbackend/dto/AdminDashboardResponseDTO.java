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
public class AdminDashboardResponseDTO {
    private DashboardSummaryDTO summary;
    private List<UserRegistrationTrendDTO> userRegistrationTrend;
    private List<UserRoleDistributionDTO> userRolesData;
    private List<RecentActivityDTO> recentActivity;
    private List<MonthlyDataDTO> monthlyOTCData;
    private List<MonthlyDataDTO> pharmacyOnboardingData;
}
