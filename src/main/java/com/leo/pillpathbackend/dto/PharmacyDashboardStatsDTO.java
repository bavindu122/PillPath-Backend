package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDashboardStatsDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalInventoryItems;
    private Long activeStaffMembers;
    private Long pendingOrders;
    private Long completedOrders;
    private Long lowStockItems;
    private Long outOfStockItems;
}