package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.PharmacistService;
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
@RequestMapping("/api/v1/pharmacist")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
public class PharmacistProfileController {

    private final PharmacistService pharmacistService;
    private final CloudinaryService cloudinaryService;
    private final AuthenticationHelper authHelper;

    /**
     * Get current pharmacist's profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        try {
            Long pharmacistId = authHelper.extractPharmacistIdFromRequest(request);
            log.info("GET request for pharmacist profile, ID: {}", pharmacistId);
            
            PharmacistResponseDTO profile = pharmacistService.getPharmacistById(pharmacistId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.error("Error fetching pharmacist profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching pharmacist profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Update current pharmacist's profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody PharmacistUpdateRequestDTO updateRequest, HttpServletRequest request) {
        try {
            Long pharmacistId = authHelper.extractPharmacistIdFromRequest(request);
            log.info("PUT request to update pharmacist profile, ID: {}", pharmacistId);
            
            PharmacistResponseDTO updatedProfile = pharmacistService.updatePharmacist(pharmacistId, updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            log.error("Error updating pharmacist profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating pharmacist profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Upload profile picture for current pharmacist
     */
    @PostMapping("/profile/picture")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            Long pharmacistId = authHelper.extractPharmacistIdFromRequest(request);
            log.info("POST request to upload profile picture for pharmacist ID: {}", pharmacistId);

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
            Map<String, Object> uploadResult = cloudinaryService.uploadProfilePicture(file, pharmacistId);
            String imageUrl = (String) uploadResult.get("secure_url");

            // Update pharmacist with new profile picture URL
            PharmacistResponseDTO updatedProfile = pharmacistService.updateProfilePicture(pharmacistId, imageUrl);

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
     * Delete profile picture for current pharmacist
     */
    @DeleteMapping("/profile/picture")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> deleteProfilePicture(HttpServletRequest request) {
        try {
            Long pharmacistId = authHelper.extractPharmacistIdFromRequest(request);
            log.info("DELETE request to remove profile picture for pharmacist ID: {}", pharmacistId);

            PharmacistResponseDTO updatedProfile = pharmacistService.removeProfilePicture(pharmacistId);

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
