package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.PharmacistCreateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistUpdateRequestDTO;
import com.leo.pillpathbackend.dto.PharmacistResponseDTO;
import com.leo.pillpathbackend.dto.order.PharmacyOrderItemDTO;
import com.leo.pillpathbackend.dto.order.OrderTotalsDTO;
import com.leo.pillpathbackend.dto.order.PaymentDTO;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.repository.PharmacyOrderRepository;
import com.leo.pillpathbackend.service.PharmacistService;
import com.leo.pillpathbackend.service.WalletSettingsService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.leo.pillpathbackend.repository.PharmacyAdminRepository;

@RestController
@RequestMapping("/api/v1/pharmacy-admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PharmacyAdminController {

    private final PharmacistService pharmacistService;

    private final AuthenticationHelper auth;
    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final WalletSettingsService walletSettingsService;
    private final PharmacyAdminRepository pharmacyAdminRepository;

    private static final RoundingMode RM = RoundingMode.HALF_UP;

    private Map<String, Object> toAdminOrderDTO(PharmacyOrder po) {
        CustomerOrder parent = po.getCustomerOrder();
        Customer cust = parent != null ? parent.getCustomer() : null;
        BigDecimal total = Optional.ofNullable(po.getTotal()).orElseGet(() -> Optional.ofNullable(po.getItems()).orElse(List.of()).stream()
                .map(PharmacyOrderItem::getTotalPrice).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal commissionPercent = walletSettingsService.resolveCommissionPercent(po.getPharmacy().getId());
        if (commissionPercent == null) commissionPercent = BigDecimal.ZERO;
        BigDecimal commissionAmount = total.multiply(commissionPercent).divide(new BigDecimal("100"), 2, RM);
        BigDecimal convenienceFee = Optional.ofNullable(walletSettingsService.getSettings().getConvenienceFee()).orElse(BigDecimal.ZERO).setScale(2, RM);
        BigDecimal netAfterCommission = total.subtract(commissionAmount).setScale(2, RM);

        List<PharmacyOrderItemDTO> itemDTOs = Optional.ofNullable(po.getItems()).orElse(List.of()).stream().map(it -> PharmacyOrderItemDTO.builder()
                .itemId(it.getId())
                .previewItemId(it.getSubmissionItem() != null ? it.getSubmissionItem().getId() : null)
                .medicineName(it.getMedicineName())
                .genericName(it.getGenericName())
                .dosage(it.getDosage())
                .quantity(it.getQuantity())
                .unitPrice(it.getUnitPrice())
                .totalPrice(it.getTotalPrice())
                .notes(it.getSubmissionItem() != null ? it.getSubmissionItem().getNotes() : null)
                .build()).toList();

        Map<String, Object> fees = new LinkedHashMap<>();
        fees.put("platformCommissionPercent", commissionPercent);
        fees.put("platformCommissionAmount", commissionAmount);
        fees.put("convenienceFee", convenienceFee);
        fees.put("netAfterCommission", netAfterCommission);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("pharmacyOrderId", po.getId());
        dto.put("orderCode", po.getOrderCode());
        dto.put("status", po.getStatus());
        dto.put("createdAt", po.getCreatedAt());
        dto.put("updatedAt", po.getUpdatedAt());
        dto.put("completedDate", (po.getStatus() == PharmacyOrderStatus.HANDED_OVER && po.getUpdatedAt() != null) ? po.getUpdatedAt() : null);
        dto.put("pharmacyId", po.getPharmacy().getId());
        dto.put("pharmacyName", po.getPharmacy().getName());
        dto.put("pickupLocation", po.getPickupLocation());
        dto.put("pickupCode", po.getPickupCode());
        dto.put("payment", PaymentDTO.builder()
                .method(parent != null ? parent.getPaymentMethod() : null)
                .status(parent != null ? parent.getPaymentStatus() : null)
                .amount(total)
                .currency(parent != null ? parent.getCurrency() : "LKR")
                .reference(parent != null ? parent.getPaymentReference() : null)
                .build());
        dto.put("totals", OrderTotalsDTO.builder()
                .subtotal(po.getSubtotal())
                .discount(po.getDiscount())
                .tax(po.getTax())
                .shipping(po.getShipping())
                .total(total)
                .currency(parent != null ? parent.getCurrency() : "LKR")
                .build());
        dto.put("customerName", cust != null ? cust.getFullName() : null);
        dto.put("customerEmail", cust != null ? cust.getEmail() : null);
        dto.put("customerPhone", cust != null ? cust.getPhoneNumber() : null);
        dto.put("items", itemDTOs);
        dto.put("fees", fees);
        return dto;
    }

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
    public ResponseEntity<?> addStaffMember(@Valid @RequestBody PharmacistCreateRequestDTO request) {
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
            @Valid @RequestBody PharmacistUpdateRequestDTO request) {
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

    /**
     * List pharmacy orders for admin
     */
    @GetMapping("/pharmacies/{pharmacyId}/orders")
    public ResponseEntity<?> listPharmacyOrdersAdmin(@PathVariable Long pharmacyId,
                                                     @RequestParam(name = "status", required = false) PharmacyOrderStatus status,
                                                     HttpServletRequest request) {
        try {
            String token = auth.extractAndValidateToken(request);
            if (token == null) throw new IllegalArgumentException("Missing or invalid authorization header");
            Long adminId = auth.extractPharmacyAdminIdFromToken(token);
            PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacy admin not found"));
            if (admin.getPharmacy() == null || !admin.getPharmacy().getId().equals(pharmacyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized for this pharmacy"));
            }
            List<PharmacyOrder> list = (status == null)
                    ? pharmacyOrderRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId)
                    : pharmacyOrderRepository.findByPharmacyIdAndStatusOrderByCreatedAtDesc(pharmacyId, status);
            List<Map<String, Object>> dtos = list.stream().map(this::toAdminOrderDTO).toList();
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Error listing pharmacy orders for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pharmacy order details for admin
     */
    @GetMapping("/pharmacies/{pharmacyId}/orders/{orderId}")
    public ResponseEntity<?> getPharmacyOrderAdmin(@PathVariable Long pharmacyId,
                                                   @PathVariable Long orderId,
                                                   HttpServletRequest request) {
        try {
            String token = auth.extractAndValidateToken(request);
            if (token == null) throw new IllegalArgumentException("Missing or invalid authorization header");
            Long adminId = auth.extractPharmacyAdminIdFromToken(token);
            PharmacyAdmin admin = pharmacyAdminRepository.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacy admin not found"));
            if (admin.getPharmacy() == null || !admin.getPharmacy().getId().equals(pharmacyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized for this pharmacy"));
            }
            PharmacyOrder po = pharmacyOrderRepository.findByIdAndPharmacyId(orderId, pharmacyId)
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacy order not found"));
            return ResponseEntity.ok(toAdminOrderDTO(po));
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Error fetching pharmacy order for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}

