package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/pharmacies")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PharmacyManagementController {

    private final PharmacyService pharmacyService;

    @GetMapping
    public ResponseEntity<Page<PharmacyDTO>> getAllPharmacies(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "All") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PharmacyDTO> pharmacies = pharmacyService.getAllPharmacies(search, status, pageable);

        return ResponseEntity.ok(pharmacies);
    }

    @GetMapping("/stats")
    public ResponseEntity<PharmacyStatsDTO> getPharmacyStats() {
        PharmacyStatsDTO stats = pharmacyService.getPharmacyStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PharmacyDTO> getPharmacyById(@PathVariable Long id) {
        try {
            PharmacyDTO pharmacy = pharmacyService.getPharmacyById(id);
            return ResponseEntity.ok(pharmacy);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approvePharmacy(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PharmacyDTO pharmacy = pharmacyService.approvePharmacy(id);
            response.put("success", true);
            response.put("message", "Pharmacy approved successfully");
            response.put("pharmacy", pharmacy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectPharmacy(
            @PathVariable Long id,
            @RequestBody PharmacyManagementDTO request) {

        Map<String, Object> response = new HashMap<>();
        try {
            PharmacyDTO pharmacy = pharmacyService.rejectPharmacy(id, request.getReason());
            response.put("success", true);
            response.put("message", "Pharmacy rejected successfully");
            response.put("pharmacy", pharmacy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendPharmacy(
            @PathVariable Long id,
            @RequestBody PharmacyManagementDTO request) {

        Map<String, Object> response = new HashMap<>();
        try {
            PharmacyDTO pharmacy = pharmacyService.suspendPharmacy(id, request.getReason());
            response.put("success", true);
            response.put("message", "Pharmacy suspended successfully");
            response.put("pharmacy", pharmacy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activatePharmacy(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PharmacyDTO pharmacy = pharmacyService.activatePharmacy(id);
            response.put("success", true);
            response.put("message", "Pharmacy activated successfully");
            response.put("pharmacy", pharmacy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePharmacy(
            @PathVariable Long id,
            @RequestBody PharmacyDTO pharmacyDTO) {

        Map<String, Object> response = new HashMap<>();
        try {
            PharmacyDTO updatedPharmacy = pharmacyService.updatePharmacyDetails(id, pharmacyDTO);
            response.put("success", true);
            response.put("message", "Pharmacy updated successfully");
            response.put("pharmacy", updatedPharmacy);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}