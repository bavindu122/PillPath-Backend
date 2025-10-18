package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.wallet.WalletSettingsDTO;
import com.leo.pillpathbackend.entity.CommissionRule;
import com.leo.pillpathbackend.entity.PlatformSettings;
import com.leo.pillpathbackend.service.WalletSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminWalletSettingsController {

    private final WalletSettingsService settingsService;

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        PlatformSettings s = settingsService.getSettings();
        WalletSettingsDTO dto = new WalletSettingsDTO();
        dto.setCurrency(s.getCurrency());
        dto.setCommissionPercent(s.getCommissionPercent());
        dto.setConvenienceFee(s.getConvenienceFee());
        dto.setVersion(s.getVersion());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody WalletSettingsDTO dto) {
        try {
            PlatformSettings s = settingsService.updateSettings(dto.getCurrency(), dto.getCommissionPercent(), dto.getConvenienceFee(), dto.getVersion());
            WalletSettingsDTO out = new WalletSettingsDTO();
            out.setCurrency(s.getCurrency());
            out.setCommissionPercent(s.getCommissionPercent());
            out.setConvenienceFee(s.getConvenienceFee());
            out.setVersion(s.getVersion());
            return ResponseEntity.ok(out);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/commission/pharmacies/{pharmacyId}")
    public ResponseEntity<?> getCommission(@PathVariable Long pharmacyId) {
        CommissionRule r = settingsService.getCommissionRule(pharmacyId);
        if (r == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No override for pharmacy"));
        return ResponseEntity.ok(Map.of("pharmacyId", pharmacyId, "commissionPercent", r.getCommissionPercent(), "version", r.getVersion()));
    }

    @PutMapping("/commission/pharmacies/{pharmacyId}")
    public ResponseEntity<?> upsertCommission(@PathVariable Long pharmacyId, @RequestBody Map<String, Object> body) {
        try {
            BigDecimal percent = body.get("commissionPercent") != null ? new BigDecimal(body.get("commissionPercent").toString()) : null;
            Long version = body.get("version") != null ? Long.valueOf(body.get("version").toString()) : null;
            if (percent == null) return ResponseEntity.badRequest().body(Map.of("error", "commissionPercent required"));
            CommissionRule r = settingsService.upsertCommissionRule(pharmacyId, percent, version);
            return ResponseEntity.ok(Map.of("pharmacyId", pharmacyId, "commissionPercent", r.getCommissionPercent(), "version", r.getVersion()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/commission/pharmacies/{pharmacyId}")
    public ResponseEntity<?> deleteCommission(@PathVariable Long pharmacyId) {
        settingsService.deleteCommissionRule(pharmacyId);
        return ResponseEntity.noContent().build();
    }
}

