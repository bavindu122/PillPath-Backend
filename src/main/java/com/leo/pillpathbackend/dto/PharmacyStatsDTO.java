package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStatsDTO {
    private Long activePharmacies;
    private Long rejectedPharmacies;
    private Long pendingApproval;
    private Long suspendedPharmacies;
}