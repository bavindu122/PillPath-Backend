package com.leo.pillpathbackend.dto.order;

import lombok.Data;

/**
 * Represents a selected submission item (NOT the submission itself) within a pharmacy order request.
 * Field name kept as submissionId to match incoming JSON, but it actually refers to a PrescriptionSubmissionItem id.
 */
@Data
public class PharmacyOrderItemSelectionDTO {
    private Long submissionId; // actually the submission item id
    private Integer quantity;  // optional override quantity (falls back to original submission item quantity)
}

