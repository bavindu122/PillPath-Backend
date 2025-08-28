package com.leo.pillpathbackend.dto.activity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PrescriptionActivityDTO {
    private String code; // previously prescriptionId; expose external code to customer
    private String uploadedAt; // ISO string
    private String imageUrl;
    private List<PharmacyActivityDTO> pharmacies;
}
