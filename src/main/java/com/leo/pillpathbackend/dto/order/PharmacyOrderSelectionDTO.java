package com.leo.pillpathbackend.dto.order;

import lombok.Data;
import java.util.List;

@Data
public class PharmacyOrderSelectionDTO {
    private Long pharmacyId;
    private Long submissionId; // link to prescription submission
    private List<Long> itemIds; // selected submission item ids
    private String customerNote; // optional note per pharmacy
}

