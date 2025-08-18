package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy-admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PharmacistController {

    private final PharmacistService pharmacistService;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> createStaffMember(@Valid @RequestBody PharmacistCreateRequestDTO request) {
        try {
            log.info("POST request to create staff member with email: {}", request.getEmail());
            log.info("Request data: {}", request);

            PharmacistResponseDTO response = pharmacistService.createPharmacist(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error creating staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating staff member: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/pharmacies/{pharmacyId}/staff")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> getPharmacyStaff(@PathVariable Long pharmacyId) {
        try {
            log.info("GET request to fetch staff for pharmacy ID: {}", pharmacyId);
            List<PharmacistResponseDTO> staff = pharmacistService.getPharmacistsByPharmacyId(pharmacyId);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            log.error("Error fetching pharmacy staff for pharmacy ID {}: {}", pharmacyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> updateStaffMember(
            @PathVariable Long staffId,
            @Valid @RequestBody PharmacistUpdateRequestDTO request) {
        try {
            log.info("PUT request to update staff member ID: {}", staffId);
            PharmacistResponseDTO response = pharmacistService.updatePharmacist(staffId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating staff member ID {}: {}", staffId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating staff member ID {}: ", staffId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> deleteStaffMember(@PathVariable Long staffId) {
        try {
            log.info("DELETE request for staff member ID: {}", staffId);
            pharmacistService.deletePharmacist(staffId);
            return ResponseEntity.ok(Map.of("message", "Staff member deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting staff member ID {}: {}", staffId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting staff member ID {}: ", staffId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> getStaffMember(@PathVariable Long staffId) {
        try {
            log.info("GET request for staff member ID: {}", staffId);
            PharmacistResponseDTO response = pharmacistService.getPharmacistById(staffId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error fetching staff member ID {}: {}", staffId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching staff member ID {}: ", staffId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/pharmacies/{pharmacyId}/staff/count")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> getStaffCount(@PathVariable Long pharmacyId) {
        try {
            log.info("GET request for staff count of pharmacy ID: {}", pharmacyId);
            long count = pharmacistService.getPharmacistCountByPharmacyId(pharmacyId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error getting staff count for pharmacy ID {}: {}", pharmacyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/staff/{staffId}/toggle-status")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> toggleStaffStatus(
            @PathVariable Long staffId,
            @RequestBody Map<String, Boolean> statusRequest) {
        try {
            log.info("PATCH request to toggle status for staff ID: {}", staffId);
            Boolean isActive = statusRequest.get("isActive");
            PharmacistResponseDTO response = pharmacistService.togglePharmacistStatus(staffId, isActive);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error toggling staff status for ID {}: {}", staffId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error toggling staff status for ID {}: ", staffId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/pharmacies/{pharmacyId}/staff/search")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> searchStaff(
            @PathVariable Long pharmacyId,
            @RequestParam(required = false) String term) {
        try {
            log.info("GET request to search staff for pharmacy ID: {} with term: {}", pharmacyId, term);
            List<PharmacistResponseDTO> staff = pharmacistService.searchPharmacistsByPharmacyId(pharmacyId, term);
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            log.error("Error searching staff for pharmacy ID {}: {}", pharmacyId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}