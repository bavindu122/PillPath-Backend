package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.*;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.PharmacyService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pharmacies")
@CrossOrigin(origins = "*")
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final AuthenticationHelper authenticationHelper;
    private final CloudinaryService cloudinaryService;
    // New: repositories for reviews listing
    private final com.leo.pillpathbackend.repository.PharmacyReviewRepository pharmacyReviewRepository;
    private final com.leo.pillpathbackend.repository.UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<PharmacyRegistrationResponse> registerPharmacy(
            @RequestBody PharmacyRegistrationRequest request) {
        try {
            PharmacyRegistrationResponse response = pharmacyService.registerPharmacy(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            PharmacyRegistrationResponse errorResponse = new PharmacyRegistrationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @GetMapping(value = "/admin/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPharmacyAdminProfile(HttpServletRequest request) {
        try {
            Long adminId = authenticationHelper.extractPharmacyAdminIdFromRequest(request);
            PharmacyAdminProfileDTO profile = pharmacyService.getPharmacyAdminProfileById(adminId);
            return ResponseEntity.ok(profile);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Admin not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get profile: " + e.getMessage()));
        }
    }
    @PutMapping("/admin/profile")
    public ResponseEntity<Map<String, Object>> updatePharmacyAdminProfile(
            @RequestBody PharmacyAdminProfileDTO profileDTO,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long adminId = authenticationHelper.extractPharmacyAdminIdFromRequest(request);
            PharmacyAdminProfileDTO updatedProfile = pharmacyService.updatePharmacyAdminProfile(adminId, profileDTO);

            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("admin", updatedProfile);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Admin-only endpoint for verifying pharmacies
    @PutMapping("/admin/verify/{pharmacyId}")
    public ResponseEntity<Map<String, Object>> verifyPharmacy(
            @PathVariable Long pharmacyId,
            @RequestParam boolean approved,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: Add system admin authentication here
            boolean result = pharmacyService.verifyPharmacy(pharmacyId, approved);

            response.put("success", true);
            response.put("verified", result);
            response.put("message", approved ? "Pharmacy verified successfully" : "Pharmacy verification rejected");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/map")
    public ResponseEntity<List<PharmacyMapDTO>> getPharmaciesForMap(
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            @RequestParam(defaultValue = "10") Double radiusKm) {

        List<PharmacyMapDTO> pharmacies = pharmacyService.getPharmaciesForMap(userLat, userLng, radiusKm);
        return ResponseEntity.ok(pharmacies);
    }


    @GetMapping("/pharmacy-profile")
    public ResponseEntity<?> getPharmacyProfile(HttpServletRequest request) {
        try {
            Long adminId = authenticationHelper.extractPharmacyAdminIdFromRequest(request);
            PharmacyDTO pharmacyProfile = pharmacyService.getPharmacyProfileByAdminId(adminId);

            return ResponseEntity.ok(pharmacyProfile);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get pharmacy profile: " + e.getMessage()));
        }
    }

    @PutMapping("/pharmacy-profile")
    public ResponseEntity<Map<String, Object>> updatePharmacyProfile(
            @RequestBody PharmacyDTO pharmacyDTO,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long adminId = authenticationHelper.extractPharmacyAdminIdFromRequest(request);
            PharmacyDTO updatedProfile = pharmacyService.updatePharmacyProfile(adminId, pharmacyDTO);

            response.put("success", true);
            response.put("message", "Pharmacy profile updated successfully");
            response.put("pharmacy", updatedProfile);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update pharmacy profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // New: upload or change pharmacy logo
    @PostMapping(value = "/pharmacy-profile/upload-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPharmacyLogo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Long adminId = authenticationHelper.extractPharmacyAdminIdFromRequest(request);

            // Load current pharmacy to get old publicId
            PharmacyDTO current = pharmacyService.getPharmacyProfileByAdminId(adminId);
            String oldPublicId = current.getLogoPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteImage(oldPublicId);
            }

            // Upload new logo
            Map<String, Object> upload = cloudinaryService.uploadPharmacyLogo(file, current.getId());
            String newUrl = upload.get("secure_url").toString();
            String newPublicId = upload.get("public_id").toString();

            // Persist new logo details
            PharmacyDTO patch = new PharmacyDTO();
            patch.setLogoUrl(newUrl);
            patch.setLogoPublicId(newPublicId);
            PharmacyDTO updated = pharmacyService.updatePharmacyProfile(adminId, patch);

            response.put("success", true);
            response.put("imageUrl", newUrl);
            response.put("message", "Logo updated successfully");
            response.put("pharmacy", updated);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to upload logo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // New: upload or change pharmacy banner
    @PostMapping(value = "/pharmacy-profile/upload-banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPharmacyBanner(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Long adminId = authenticationHelper.extractPharmacyAdminIdFromRequest(request);

            // Load current pharmacy to get old publicId
            PharmacyDTO current = pharmacyService.getPharmacyProfileByAdminId(adminId);
            String oldPublicId = current.getBannerPublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                cloudinaryService.deleteImage(oldPublicId);
            }

            // Upload new banner
            Map<String, Object> upload = cloudinaryService.uploadPharmacyBanner(file, current.getId());
            String newUrl = upload.get("secure_url").toString();
            String newPublicId = upload.get("public_id").toString();

            // Persist new banner details
            PharmacyDTO patch = new PharmacyDTO();
            patch.setBannerUrl(newUrl);
            patch.setBannerPublicId(newPublicId);
            PharmacyDTO updated = pharmacyService.updatePharmacyProfile(adminId, patch);

            response.put("success", true);
            response.put("imageUrl", newUrl);
            response.put("message", "Banner updated successfully");
            response.put("pharmacy", updated);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to upload banner: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{pharmacyId}/profile")
    public ResponseEntity<?> getPharmacyProfile(@PathVariable Long pharmacyId) {
        try {
            System.out.println("Fetching pharmacy profile for ID: " + pharmacyId);
            PharmacyDTO profile = pharmacyService.getPharmacyProfile(pharmacyId);
            System.out.println("Profile found: " + profile.getName());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(profile);
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", e.getMessage(), "status", 404));
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Failed to get pharmacy profile: " + e.getMessage(), "status", 500));
        }
    }

    @GetMapping("/{pharmacyId}/products")
    public ResponseEntity<?> getPharmacyProducts(@PathVariable Long pharmacyId) {
        try {
            System.out.println("Fetching pharmacy products for ID: " + pharmacyId);
            List<OTCProductDTO> products = pharmacyService.getPharmacyProducts(pharmacyId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(products);
        } catch (Exception e) {
            System.err.println("Error fetching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Failed to get pharmacy products: " + e.getMessage(), "status", 500));
        }
    }

    @GetMapping("/{pharmacyId}/reviews")
    public ResponseEntity<?> listPharmacyReviews(@PathVariable("pharmacyId") Long pharmacyId) {
        try {
            List<com.leo.pillpathbackend.entity.PharmacyReview> reviews = pharmacyReviewRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId);
            List<Map<String, Object>> items = reviews.stream().map(r -> {
                String displayName = "Anonymous";
                if (r.getCustomerId() != null) {
                    displayName = userRepository.findById(r.getCustomerId())
                            .map(u -> {
                                String fn = u.getFullName();
                                if (fn != null && !fn.isBlank()) return fn; // prefer full_name
                                String un = u.getUsername();
                                return (un != null && !un.isBlank()) ? un : "User";
                            })
                            .orElse("Anonymous");
                }
                String iso = r.getCreatedAt() != null
                        ? r.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).withZoneSameInstant(java.time.ZoneId.of("UTC")).toInstant().toString()
                        : null;
                return java.util.Map.<String, Object>of(
                        "id", r.getReviewId(),
                        "userName", displayName,
                        "date", iso,
                        "rating", r.getRating(),
                        "comment", r.getReview(),
                        "helpfulCount", 0
                );
            }).toList();
            return ResponseEntity.ok(Map.of("items", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}