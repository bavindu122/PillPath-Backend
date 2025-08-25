package com.leo.pillpathbackend.dto.activity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PrescriptionActivityListResponse {
    private List<PrescriptionActivityDTO> items;
    private PageMeta page; // nullable

    @Data
    @Builder
    public static class PageMeta {
        private int page;
        private int size;
        private long total;
    }
}

