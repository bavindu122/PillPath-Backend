package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private Long totalUsers;
    private Long activePharmacies;
    private Long pendingPrescriptions;
    private Long completedOrders;
    private Double totalRevenue;
    private Double walletBalance;
}