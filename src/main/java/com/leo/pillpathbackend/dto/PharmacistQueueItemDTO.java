package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacistQueueItemDTO {
    private Long submissionId;          // per-pharmacy submission (routing) id
    private String prescriptionCode;    // shared code across pharmacies
    private String uploadedAt;          // ISO timestamp
    private PrescriptionStatus status;  // submission status
    private String imageUrl;            // image
    private String note;                // note provided by customer
    private Boolean claimed;            // whether claimed by a pharmacist
    private Long assignedPharmacistId;  // who claimed
    private String customerName;        // name of the customer
}
