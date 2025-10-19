package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.service.PrescriptionService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LegacyPrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AuthenticationHelper auth;

    // Legacy path: PATCH /api/prescriptions/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateSubmissionStatus(@PathVariable("id") Long submissionId,
                                                    @RequestBody Map<String, String> body,
                                                    HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            String statusStr = body != null ? body.get("status") : null;
            if (statusStr == null || statusStr.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
            }
            com.leo.pillpathbackend.entity.enums.PrescriptionStatus status;
            try {
                status = com.leo.pillpathbackend.entity.enums.PrescriptionStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
            }
            prescriptionService.updateSubmissionStatus(pharmacistId, submissionId, status);
            return ResponseEntity.ok(Map.of("id", submissionId, "status", status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Legacy path: DELETE /api/prescriptions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubmission(@PathVariable("id") Long submissionId,
                                              HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            prescriptionService.deleteSubmission(pharmacistId, submissionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}

