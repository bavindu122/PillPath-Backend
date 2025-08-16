package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacists")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PharmacistController {

    private final PharmacistService pharmacistService;

    @PostMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<PharmacistResponseDTO> createPharmacist(
            @PathVariable Long pharmacyId,
            @Valid @RequestBody PharmacistCreateRequestDTO request) {
        request.setPharmacyId(pharmacyId);
        PharmacistResponseDTO response = pharmacistService.createPharmacist(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<PharmacistResponseDTO> updatePharmacist(
            @PathVariable Long id,
            @Valid @RequestBody PharmacistUpdateRequestDTO request) {
        PharmacistResponseDTO response = pharmacistService.updatePharmacist(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<List<PharmacistResponseDTO>> getPharmacistsByPharmacy(@PathVariable Long pharmacyId) {
        List<PharmacistResponseDTO> pharmacists = pharmacistService.getPharmacistsByPharmacy(pharmacyId);
        return ResponseEntity.ok(pharmacists);
    }

    @GetMapping("/pharmacy/{pharmacyId}/search")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<List<PharmacistResponseDTO>> searchPharmacists(
            @PathVariable Long pharmacyId,
            @RequestParam(required = false) String searchTerm) {
        List<PharmacistResponseDTO> pharmacists = pharmacistService.searchPharmacistsByPharmacyId(pharmacyId, searchTerm);
        return ResponseEntity.ok(pharmacists);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<PharmacistResponseDTO> getPharmacistById(@PathVariable Long id) {
        PharmacistResponseDTO response = pharmacistService.getPharmacistById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<PharmacistResponseDTO> togglePharmacistStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        PharmacistResponseDTO response = pharmacistService.togglePharmacistStatus(id, isActive);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pharmacy/{pharmacyId}/count")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<Long> getPharmacistCount(@PathVariable Long pharmacyId) {
        long count = pharmacistService.getPharmacistCountByPharmacyId(pharmacyId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<Void> deletePharmacist(@PathVariable Long id) {
        pharmacistService.deletePharmacist(id);
        return ResponseEntity.noContent().build();
    }
}