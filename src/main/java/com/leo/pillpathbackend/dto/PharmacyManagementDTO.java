package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyManagementDTO {
    private Long pharmacyId;
    private String action; // "approve", "reject", "suspend", "activate"
    private String reason;
}
