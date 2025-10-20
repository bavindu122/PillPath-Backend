package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacyDashboardStatsDTO;
import com.leo.pillpathbackend.service.PharmacyDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pharmacy/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PharmacyDashboardController {

    private final PharmacyDashboardService dashboardService;

    /**
     * Get dashboard statistics for a pharmacy
     * No auth required (you can add auth if needed)
     */
    @GetMapping("/{pharmacyId}/statistics")
    public ResponseEntity<?> getDashboardStatistics(@PathVariable Long pharmacyId) {
        try {
            log.info("GET request for dashboard statistics for pharmacy ID: {}", pharmacyId);
            PharmacyDashboardStatsDTO stats = dashboardService.getDashboardStatistics(pharmacyId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Pharmacy not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching dashboard statistics for pharmacy ID {}: {}", pharmacyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch dashboard statistics"));
        }
    }
}