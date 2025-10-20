package com.leo.pillpathbackend.dto.reroute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerouteRequest {
    private Long originalPharmacyId;
    private String parentPreviewId;
    private Long targetPharmacyId; // single
    private List<Long> targetPharmacyIds; // multi
    private String note;
    private List<RerouteItemRequest> items;
    private String strategy; // SINGLE or BROADCAST

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RerouteItemRequest {
        private Long previewItemId;
        private Long medicineSetId;
        private String name;
        private String genericName;
        private String dosage;
        private Integer quantity;
        private Map<String, Object> dosageMeta;
        private String notes;
    }
}

