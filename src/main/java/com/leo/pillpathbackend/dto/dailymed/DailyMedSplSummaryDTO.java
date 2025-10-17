package com.leo.pillpathbackend.dto.dailymed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyMedSplSummaryDTO {
    private String setid;          // SPL Set ID
    private String title;          // Product title
    private String effective_time; // Effective date/time string
    private String version;        // Version number as string
}

