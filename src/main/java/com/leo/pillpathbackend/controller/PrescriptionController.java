package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PrescriptionDTO;
import com.leo.pillpathbackend.dto.PrescriptionItemDTO;
import com.leo.pillpathbackend.dto.PrescriptionListItemDTO;
import com.leo.pillpathbackend.dto.request.CreatePrescriptionRequest;
import com.leo.pillpathbackend.dto.activity.PrescriptionActivityListResponse;
import com.leo.pillpathbackend.dto.PharmacistSubmissionItemsDTO;
import com.leo.pillpathbackend.dto.reroute.RerouteCandidatesResponse;
import com.leo.pillpathbackend.dto.reroute.RerouteRequest;
import com.leo.pillpathbackend.dto.reroute.RerouteResponse;
import com.leo.pillpathbackend.entity.PharmacistUser;
import com.leo.pillpathbackend.service.PrescriptionService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.leo.pillpathbackend.repository.PharmacistUserRepository;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AuthenticationHelper auth;
    private final PharmacistUserRepository pharmacistUserRepository;

    // Customer: upload a prescription image to a chosen pharmacy
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "meta", required = false) CreatePrescriptionRequest meta,
            // Fallback params if meta is not provided as JSON part
            @RequestParam(value = "pharmacyIds", required = false) List<Long> pharmacyIds, // multi support
            @RequestParam(value = "pharmacyId", required = false) Long pharmacyId,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "deliveryPreference", required = false) String deliveryPreference,
            @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) BigDecimal longitude,
            HttpServletRequest request
    ) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);

            if (meta == null) {
                meta = new CreatePrescriptionRequest();
                if (pharmacyIds != null && !pharmacyIds.isEmpty()) {
                    meta.setPharmacyIds(pharmacyIds);
                } else {
                    meta.setPharmacyId(pharmacyId);
                }
                meta.setNote(note);
                if (deliveryPreference != null) {
                    try {
                        meta.setDeliveryPreference(
                                com.leo.pillpathbackend.entity.enums.DeliveryPreference.valueOf(deliveryPreference.toUpperCase())
                        );
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Invalid deliveryPreference"));
                    }
                }
                meta.setDeliveryAddress(deliveryAddress);
                meta.setLatitude(latitude);
                meta.setLongitude(longitude);
            }

            PrescriptionDTO dto = prescriptionService.uploadPrescription(customerId, file, meta);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Image upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer: list own prescriptions
    @GetMapping("/my")
    public ResponseEntity<?> myPrescriptions(HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            List<PrescriptionListItemDTO> list = prescriptionService.getCustomerPrescriptions(customerId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer: list prescriptions for a specific family member
    @GetMapping("/family-member/{familyMemberId}")
    public ResponseEntity<?> getFamilyMemberPrescriptions(
            @PathVariable Long familyMemberId,
            HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            List<PrescriptionListItemDTO> list = prescriptionService.getFamilyMemberPrescriptions(customerId, familyMemberId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer: view a specific prescription
    @GetMapping("/my/{id}")
    public ResponseEntity<?> myPrescription(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            PrescriptionDTO dto = prescriptionService.getCustomerPrescription(id, customerId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Pharmacy staff (PHARMACIST): list prescriptions received by their pharmacy
    @GetMapping("/pharmacy")
    public ResponseEntity<?> pharmacyPrescriptions(HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            PharmacistUser pharmacist = pharmacistUserRepository.findById(pharmacistId)
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
            if (pharmacist.getPharmacy() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pharmacist is not assigned to a pharmacy"));
            }
            Long pharmacyId = pharmacist.getPharmacy().getId();
            List<PrescriptionListItemDTO> list = prescriptionService.getPharmacyPrescriptions(pharmacyId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Pharmacy staff (PHARMACIST): view one prescription
    @GetMapping("/pharmacy/{id}")
    public ResponseEntity<?> pharmacyPrescription(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            PharmacistUser pharmacist = pharmacistUserRepository.findById(pharmacistId)
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
            if (pharmacist.getPharmacy() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pharmacist is not assigned to a pharmacy"));
            }
            Long pharmacyId = pharmacist.getPharmacy().getId();
            PrescriptionDTO dto = prescriptionService.getPharmacyPrescription(id, pharmacyId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Pharmacy staff (PHARMACIST): replace prescription items (order preview)
    @PutMapping("/pharmacy/{id}/items")
    public ResponseEntity<?> replaceItems(
            @PathVariable Long id,
            @RequestBody List<PrescriptionItemDTO> items,
            HttpServletRequest request
    ) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            PharmacistUser pharmacist = pharmacistUserRepository.findById(pharmacistId)
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
            if (pharmacist.getPharmacy() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pharmacist is not assigned to a pharmacy"));
            }
            Long pharmacyId = pharmacist.getPharmacy().getId();
            PrescriptionDTO dto = prescriptionService.replaceItemsForPharmacy(pharmacyId, id, items);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer: list activities
    @GetMapping("/activities")
    public ResponseEntity<?> customerActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            PrescriptionActivityListResponse resp = prescriptionService.getCustomerActivities(customerId, page, size);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error in customerActivities", e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", msg));
        }
    }

    @GetMapping("/pharmacist/queue")
    public ResponseEntity<?> pharmacistQueue(@RequestParam(value = "status", required = false) String status,
                                             HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            com.leo.pillpathbackend.entity.enums.PrescriptionStatus st = null;
            if (status != null) {
                try {
                    st = com.leo.pillpathbackend.entity.enums.PrescriptionStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
                }
            }
            return ResponseEntity.ok(prescriptionService.getPharmacistQueue(pharmacistId, st));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pharmacist/{submissionId}/claim")
    public ResponseEntity<?> claimSubmission(@PathVariable Long submissionId, HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            return ResponseEntity.ok(prescriptionService.claimSubmission(pharmacistId, submissionId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Pharmacist: list items in a submission (order preview)
    @GetMapping("/pharmacist/submissions/{submissionId}/items")
    public ResponseEntity<?> getSubmissionItems(@PathVariable Long submissionId, HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            PharmacistSubmissionItemsDTO dto = prescriptionService.getSubmissionItems(pharmacistId, submissionId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Pharmacist: add an item
    @PostMapping("/pharmacist/submissions/{submissionId}/items")
    public ResponseEntity<?> addSubmissionItem(@PathVariable Long submissionId,
                                               @RequestBody PrescriptionItemDTO item,
                                               HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            PharmacistSubmissionItemsDTO dto = prescriptionService.addSubmissionItem(pharmacistId, submissionId, item);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Pharmacist: remove an item
    @DeleteMapping("/pharmacist/submissions/{submissionId}/items/{itemId}")
    public ResponseEntity<?> removeSubmissionItem(@PathVariable Long submissionId,
                                                  @PathVariable Long itemId,
                                                  HttpServletRequest request) {
        try {
            Long pharmacistId = auth.extractPharmacistIdFromRequest(request);
            PharmacistSubmissionItemsDTO dto = prescriptionService.removeSubmissionItem(pharmacistId, submissionId, itemId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer: fetch a single order preview by prescription code and pharmacyId
    @GetMapping("/order-preview")
    public ResponseEntity<?> getOrderPreview(@RequestParam("code") String code,
                                             @RequestParam("pharmacyId") Long pharmacyId,
                                             HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            var dto = prescriptionService.getCustomerOrderPreview(customerId, code, pharmacyId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // New: Pharmacist updates submission status directly (matches frontend PATCH /api/prescriptions/{id}/status)
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

    // New: Pharmacist deletes a submission (matches frontend DELETE /api/prescriptions/{id})
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

    // Customer: assign prescription to a family member
    @PutMapping("/{prescriptionId}/assign-family-member")
    public ResponseEntity<?> assignPrescriptionToFamilyMember(
            @PathVariable Long prescriptionId,
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            Long familyMemberId = body.get("familyMemberId");
            
            if (familyMemberId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "familyMemberId is required"));
            }
            
            prescriptionService.assignPrescriptionToFamilyMember(prescriptionId, customerId, familyMemberId);
            return ResponseEntity.ok(Map.of(
                "message", "Prescription assigned successfully",
                "prescriptionId", prescriptionId,
                "familyMemberId", familyMemberId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    // New: Reroute candidates for a customer's prescription
    @GetMapping("/customer/{prescriptionId}/reroute/candidates")
    public ResponseEntity<?> rerouteCandidates(@PathVariable Long prescriptionId,
                                               @RequestParam(value = "excludePharmacyId", required = false) Long excludePharmacyId,
                                               @RequestParam(value = "lat", required = false) Double lat,
                                               @RequestParam(value = "lng", required = false) Double lng,
                                               @RequestParam(value = "radiusKm", required = false) Double radiusKm,
                                               @RequestParam(value = "limit", required = false) Integer limit,
                                               @RequestParam(value = "offset", required = false) Integer offset,
                                               HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            RerouteCandidatesResponse resp = prescriptionService.listRerouteCandidates(customerId, prescriptionId,
                    excludePharmacyId, lat, lng, radiusKm, limit, offset);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // New: Reroute unavailable items to new pharmacies
    @PostMapping("/customer/{prescriptionId}/reroute")
    public ResponseEntity<?> rerouteUnavailable(@PathVariable Long prescriptionId,
                                                @RequestBody RerouteRequest body,
                                                @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                HttpServletRequest request) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(request);
            RerouteResponse resp = prescriptionService.rerouteUnavailableItems(customerId, prescriptionId, body, idempotencyKey);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in rerouteUnavailable", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
