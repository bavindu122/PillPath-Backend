package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacistCreateRequest;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequest;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pharmacy-admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PharmacyAdminController {

    private final PharmacistService pharmacistService;

    /**
     * Get all staff members for a pharmacy
     */
    @GetMapping("/pharmacies/{pharmacyId}/staff")
    public ResponseEntity<?> getPharmacyStaff(
            @PathVariable Long pharmacyId,
            @RequestParam(required = false) String search) {
        try {
            List<PharmacistResponseDTO> staff;
            
            if (search != null && !search.trim().isEmpty()) {
                staff = pharmacistService.searchPharmacistsByPharmacyId(pharmacyId, search);
            } else {
                staff = pharmacistService.getPharmacistsByPharmacyId(pharmacyId);
            }
            
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            log.error("Error fetching pharmacy staff: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch staff members: " + e.getMessage()));
        }
    }

    /**
     * Get a specific staff member by ID
     */
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getStaffMember(@PathVariable Long staffId) {
        try {
            PharmacistResponseDTO staff = pharmacistService.getPharmacistById(staffId);
            return ResponseEntity.ok(staff);
        } catch (RuntimeException e) {
            log.error("Error fetching staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch staff member: " + e.getMessage()));
        }
    }

    /**
     * Add a new staff member (pharmacist)
     */
    @PostMapping("/staff")
    public ResponseEntity<?> addStaffMember(@Valid @RequestBody PharmacistCreateRequest request) {
        try {
            PharmacistResponseDTO newStaff = pharmacistService.createPharmacist(request);
            log.info("Successfully created staff member with ID: {}", newStaff.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newStaff);
        } catch (RuntimeException e) {
            log.error("Error creating staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create staff member: " + e.getMessage()));
        }
    }

    /**
     * Update an existing staff member
     */
    @PutMapping("/staff/{staffId}")
    public ResponseEntity<?> updateStaffMember(
            @PathVariable Long staffId,
            @Valid @RequestBody PharmacistUpdateRequest request) {
        try {
            PharmacistResponseDTO updatedStaff = pharmacistService.updatePharmacist(staffId, request);
            log.info("Successfully updated staff member with ID: {}", staffId);
            return ResponseEntity.ok(updatedStaff);
        } catch (RuntimeException e) {
            log.error("Error updating staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update staff member: " + e.getMessage()));
        }
    }

    /**
     * Delete a staff member (soft delete)
     */
    @DeleteMapping("/staff/{staffId}")
    public ResponseEntity<?> deleteStaffMember(@PathVariable Long staffId) {
        try {
            pharmacistService.deletePharmacist(staffId);
            log.info("Successfully deleted staff member with ID: {}", staffId);
            return ResponseEntity.ok(Map.of("message", "Staff member deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting staff member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete staff member: " + e.getMessage()));
        }
    }

    /**
     * Toggle staff member active status
     */
    @PatchMapping("/staff/{staffId}/toggle-status")
    public ResponseEntity<?> toggleStaffStatus(
            @PathVariable Long staffId,
            @RequestBody Map<String, Boolean> statusRequest) {
        try {
            Boolean isActive = statusRequest.get("isActive");
            if (isActive == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "isActive field is required"));
            }
            
            PharmacistResponseDTO updatedStaff = pharmacistService.togglePharmacistStatus(staffId, isActive);
            log.info("Successfully toggled status for staff member with ID: {} to {}", staffId, isActive);
            return ResponseEntity.ok(updatedStaff);
        } catch (RuntimeException e) {
            log.error("Error toggling staff status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error toggling staff status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to toggle staff status: " + e.getMessage()));
        }
    }

    /**
     * Get staff count for a pharmacy
     */
    @GetMapping("/pharmacies/{pharmacyId}/staff/count")
    public ResponseEntity<?> getStaffCount(@PathVariable Long pharmacyId) {
        try {
            long count = pharmacistService.getPharmacistCountByPharmacyId(pharmacyId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error fetching staff count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch staff count: " + e.getMessage()));
        }
    }

    /**
     * Verify if a staff member belongs to a pharmacy
     */
    @GetMapping("/pharmacies/{pharmacyId}/staff/{staffId}/verify")
    public ResponseEntity<?> verifyStaffBelongsToPharmacy(
            @PathVariable Long pharmacyId,
            @PathVariable Long staffId) {
        try {
            boolean belongs = pharmacistService.isPharmacistInPharmacy(staffId, pharmacyId);
            return ResponseEntity.ok(Map.of("belongs", belongs));
        } catch (Exception e) {
            log.error("Error verifying staff membership: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify staff membership: " + e.getMessage()));
        }
    }
}
