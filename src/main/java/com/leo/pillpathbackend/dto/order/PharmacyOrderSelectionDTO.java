package com.leo.pillpathbackend.dto.order;

import lombok.Data;
import java.util.List;

@Data
public class PharmacyOrderSelectionDTO {
    private Long pharmacyId; // required
    private List<PharmacyOrderItemSelectionDTO> items; // required - items selected for this pharmacy
    private String note; // optional note (was customerNote)
}
