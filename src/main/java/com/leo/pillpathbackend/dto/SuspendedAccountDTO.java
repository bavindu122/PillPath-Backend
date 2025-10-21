package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendedAccountDTO {
    private String type; // "Customer" | "Pharmacy"
    private String id;   // e.g., ph_123 or usr_987
    private String name;
    private String reason; // empty string if none
    private String suspendedAt; // ISO datetime or empty string
}

