package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.LoyaltyRateDTO;
import com.leo.pillpathbackend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/loyalty-rate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<BigDecimal> getLoyaltyRate() {
        BigDecimal rate = loyaltyService.getLoyaltyPointsRate();
        return ResponseEntity.ok(rate);
    }

    @PutMapping("/loyalty-rate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<BigDecimal> updateLoyaltyRate(@RequestBody LoyaltyRateDTO request) {
        if (request.getRate() == null || request.getRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Loyalty rate must be a positive number");
        }
        
        loyaltyService.updateLoyaltyPointsRate(request.getRate());
        BigDecimal updatedRate = loyaltyService.getLoyaltyPointsRate();
        
        return ResponseEntity.ok(updatedRate);
    }
}
