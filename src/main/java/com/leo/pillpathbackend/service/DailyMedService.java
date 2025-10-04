package com.leo.pillpathbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.leo.pillpathbackend.dto.dailymed.DailyMedMediaResponse;
import com.leo.pillpathbackend.dto.dailymed.DailyMedSplPage;

public interface DailyMedService {
    DailyMedSplPage searchByDrugName(String name, int page, int size);
    DailyMedMediaResponse getMedia(String setId);
    JsonNode getLabel(String setId);
}

