package com.leo.pillpathbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.leo.pillpathbackend.dto.ApiResponse;
import com.leo.pillpathbackend.dto.dailymed.DailyMedMediaResponse;
import com.leo.pillpathbackend.dto.dailymed.DailyMedSplPage;
import com.leo.pillpathbackend.service.DailyMedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final DailyMedService dailyMedService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String,Object>>> search(
            @RequestParam String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'name' is required");
        }
        DailyMedSplPage result = dailyMedService.searchByDrugName(name.trim(), page, size);
        List<Map<String,Object>> items = result.getResults() == null ? List.of() : result.getResults().stream().map(s -> {
            Map<String,Object> m = new HashMap<>();
            m.put("setId", s.getSetid());
            m.put("title", s.getTitle());
            m.put("effectiveTime", s.getEffective_time());
            m.put("version", s.getVersion());
            return m;
        }).collect(Collectors.toList());
        Map<String,Object> payload = new HashMap<>();
        payload.put("items", items);
        payload.put("page", Map.of(
                "page", page,
                "size", size,
                "total", result.getTotal()
        ));
        return ResponseEntity.ok(ApiResponse.success(payload, "Search successful"));
    }

    @GetMapping("/{setId}/label")
    public ResponseEntity<ApiResponse<JsonNode>> getLabel(@PathVariable String setId) {
        JsonNode label = dailyMedService.getLabel(setId);
        return ResponseEntity.ok(ApiResponse.success(label, "Label fetched"));
    }

    @GetMapping("/{setId}/media")
    public ResponseEntity<ApiResponse<DailyMedMediaResponse>> getMedia(@PathVariable String setId) {
        DailyMedMediaResponse media = dailyMedService.getMedia(setId);
        return ResponseEntity.ok(ApiResponse.success(media, "Media fetched"));
    }
}

