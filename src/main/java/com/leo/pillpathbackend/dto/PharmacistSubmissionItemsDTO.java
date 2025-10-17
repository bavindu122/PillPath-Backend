package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacistSubmissionItemsDTO {
    private Long submissionId;
    private String prescriptionCode;
    private Long pharmacyId;
    private PrescriptionStatus status;
    private BigDecimal totalPrice; // aggregated from items
    private boolean editable; // whether current pharmacist can edit items
    private List<PrescriptionItemDTO> items;
}

