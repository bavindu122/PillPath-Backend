package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacyAdminProfileDTO;
import com.leo.pillpathbackend.dto.PharmacyAdminUpdateRequestDTO;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.PharmacyAdminService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pharmacy-admin")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
public class PharmacyAdminProfileController {

    private final PharmacyAdminService pharmacyAdminService;
    private final CloudinaryService cloudinaryService;
    private final AuthenticationHelper authHelper;

    /**
     * Get current pharmacy admin's profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        try {
            Long adminId = authHelper.extractPharmacyAdminIdFromRequest(request);
            log.info("GET request for pharmacy admin profile, ID: {}", adminId);
            
            PharmacyAdminProfileDTO profile = pharmacyAdminService.getPharmacyAdminById(adminId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error fetching pharmacy admin profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching pharmacy admin profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Update current pharmacy admin's profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody PharmacyAdminUpdateRequestDTO updateRequest, HttpServletRequest request) {
        try {
            Long adminId = authHelper.extractPharmacyAdminIdFromRequest(request);
            log.info("PUT request to update pharmacy admin profile, ID: {}", adminId);
            
            PharmacyAdminProfileDTO updatedProfile = pharmacyAdminService.updatePharmacyAdmin(adminId, updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            log.error("Error updating pharmacy admin profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating pharmacy admin profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Upload profile picture for current pharmacy admin
     */
    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            Long adminId = authHelper.extractPharmacyAdminIdFromRequest(request);
            log.info("POST request to upload profile picture for pharmacy admin ID: {}", adminId);

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "File is required"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "File must be an image"));
            }

            // Validate file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "File size must be less than 5MB"));
            }

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadProfilePicture(file, adminId);
            String imageUrl = (String) uploadResult.get("secure_url");

            // Update pharmacy admin with new profile picture URL
            PharmacyAdminProfileDTO updatedProfile = pharmacyAdminService.updateProfilePicture(adminId, imageUrl);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture updated successfully",
                    "imageUrl", imageUrl,
                    "profile", updatedProfile
            ));
        } catch (RuntimeException e) {
            log.error("Error uploading profile picture: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading profile picture: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload profile picture"));
        }
    }

    /**
     * Delete profile picture for current pharmacy admin
     */
    @DeleteMapping("/profile/picture")
    @PreAuthorize("hasRole('PHARMACY_ADMIN')")
    public ResponseEntity<?> deleteProfilePicture(HttpServletRequest request) {
        try {
            Long adminId = authHelper.extractPharmacyAdminIdFromRequest(request);
            log.info("DELETE request to remove profile picture for pharmacy admin ID: {}", adminId);

            PharmacyAdminProfileDTO updatedProfile = pharmacyAdminService.removeProfilePicture(adminId);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture removed successfully",
                    "profile", updatedProfile
            ));
        } catch (RuntimeException e) {
            log.error("Error removing profile picture: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error removing profile picture: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove profile picture"));
        }
    }
}
