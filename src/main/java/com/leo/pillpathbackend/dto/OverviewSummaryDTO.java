package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewSummaryDTO {
    private int totalUsers;
    private int activePharmacies;
    private int prescriptionsUploaded;
    private int completedOrders;
    private double totalRevenue;
    private double walletBalance;
    private String currency;
}

