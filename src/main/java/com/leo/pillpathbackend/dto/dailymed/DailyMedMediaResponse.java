package com.leo.pillpathbackend.dto.dailymed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyMedMediaResponse {

    @JsonProperty("data")
    private List<MediaItem> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaItem {
        private String type;          // e.g. image, pdf
        private String url;           // media URL
        private String title;         // optional
        @JsonProperty("thumbnail")
        private String thumbnailUrl;  // optional
    }
}

