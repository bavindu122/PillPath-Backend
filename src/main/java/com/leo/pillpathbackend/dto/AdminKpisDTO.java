package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminKpisDTO {
    private int totalUsers;
    private int totalPharmacies;
    private int totalPrescriptionsUploaded;
    private int ordersProcessed;
    private int activePharmacies;
    private int suspendedPharmacies;
    private double totalPayments;
}
