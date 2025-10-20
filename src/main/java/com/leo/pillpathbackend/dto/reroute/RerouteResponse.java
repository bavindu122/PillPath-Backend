package com.leo.pillpathbackend.dto.reroute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerouteResponse {
    private String rerouteRequestId;
    private List<Created> created;
    private List<Skipped> skipped;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Created {
        private Long pharmacyId;
        private Long submissionId;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Skipped {
        private Long pharmacyId;
        private String reason;
    }
}

