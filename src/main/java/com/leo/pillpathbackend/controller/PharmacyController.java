package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacyRegistrationRequest;
import com.leo.pillpathbackend.dto.PharmacyRegistrationResponse;
import com.leo.pillpathbackend.dto.PharmacyAdminProfileDTO;
import com.leo.pillpathbackend.service.PharmacyService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pharmacies")
@CrossOrigin(origins = "*")
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final AuthenticationHelper authenticationHelper;

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
            String token = authenticationHelper.extractAndValidateToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid authorization header"));
            }

            // Extract admin ID from token (update token format for pharmacy admins)
            if (!token.startsWith("temp-token-pharmacy-")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token format"));
            }

            Long adminId = Long.parseLong(token.replace("temp-token-pharmacy-", ""));
            PharmacyAdminProfileDTO profile = pharmacyService.getPharmacyAdminProfileById(adminId);
            return ResponseEntity.ok(profile);

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
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
            String token = authenticationHelper.extractAndValidateToken(request);
            if (token == null) {
                response.put("success", false);
                response.put("message", "Missing authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extract admin ID from token
            if (!token.startsWith("temp-token-pharmacy-")) {
                response.put("success", false);
                response.put("message", "Invalid token format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Long adminId = Long.parseLong(token.replace("temp-token-pharmacy-", ""));
            PharmacyAdminProfileDTO updatedProfile = pharmacyService.updatePharmacyAdminProfile(adminId, profileDTO);

            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("admin", updatedProfile);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Invalid token");
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
}