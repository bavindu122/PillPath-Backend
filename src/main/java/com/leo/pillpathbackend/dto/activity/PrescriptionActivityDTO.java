package com.leo.pillpathbackend.dto.activity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PrescriptionActivityDTO {
    private String code; // external prescription code
    private String uploadedAt; // ISO string
    private String imageUrl;
    private String prescriptionStatus; // new: overall prescription status
    private List<PharmacyActivityDTO> pharmacies;
}
