package com.leo.pillpathbackend.dto.dailymed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyMedSplPage {

    @JsonProperty("data")
    private List<DailyMedSplSummaryDTO> results;

    // DailyMed response contains paging metadata in "metadata" or "meta" depending on endpoint
    @JsonProperty("metadata")
    private Meta meta; // will be null if not present

    @JsonProperty("meta")
    private Meta metaAlt; // fallback name

    public int getTotal() {
        Meta m = meta != null ? meta : metaAlt;
        return m != null && m.getTotal() != null ? m.getTotal() : (results != null ? results.size() : 0);
    }

    public Integer getCurrentPage() {
        Meta m = meta != null ? meta : metaAlt;
        return m != null ? m.getCurrentPage() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        @JsonProperty("current_page")
        private Integer currentPage;
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("pagesize")
        private Integer pageSize;
    }
}

