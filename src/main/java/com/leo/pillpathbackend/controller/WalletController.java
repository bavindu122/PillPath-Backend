package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.wallet.WalletSummaryDTO;
import com.leo.pillpathbackend.dto.wallet.WalletTransactionDTO;
import com.leo.pillpathbackend.dto.wallet.events.CustomerCardCapturedEvent;
import com.leo.pillpathbackend.dto.wallet.events.CustomerCashCollectedEvent;
import com.leo.pillpathbackend.dto.wallet.events.PharmacyCommissionCardCapturedEvent;
import com.leo.pillpathbackend.dto.wallet.events.RefundFullEvent;
import com.leo.pillpathbackend.entity.PharmacistUser;
import com.leo.pillpathbackend.entity.PharmacyAdmin;
import com.leo.pillpathbackend.entity.Wallet;
import com.leo.pillpathbackend.entity.WalletTransaction;
import com.leo.pillpathbackend.repository.UserRepository;
import com.leo.pillpathbackend.service.WalletService;
import com.leo.pillpathbackend.service.WalletSettingsService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final WalletSettingsService walletSettingsService;
    private final AuthenticationHelper auth;
    private final UserRepository userRepository;

    private WalletTransactionDTO toDTO(WalletTransaction t) {
        return WalletTransactionDTO.builder()
                .id(t.getId())
                .type(t.getType())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .currency(t.getCurrency())
                .orderCode(t.getOrderCode())
                .pharmacyOrderId(t.getPharmacyOrderId())
                .prescriptionId(t.getPrescriptionId())
                .paymentId(t.getPaymentId())
                .externalKey(t.getExternalKey())
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .build();
    }

    @GetMapping("/platform")
    public ResponseEntity<?> platformWallet(@RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        Wallet w = walletService.getOrCreatePlatformWallet();
        List<WalletTransactionDTO> txns = walletService.listRecentTransactionsForPlatform(pageSize).stream().map(this::toDTO).toList();
        return ResponseEntity.ok(WalletSummaryDTO.builder()
                .ownerType("PLATFORM")
                .ownerId(null)
                .currency(w.getCurrency())
                .balance(w.getBalance())
                .transactions(txns)
                .build());
    }

    @GetMapping("/my")
    public ResponseEntity<?> myWallet(@RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                      HttpServletRequest request) {
        try {
            String token = auth.extractAndValidateToken(request);
            if (token == null) throw new IllegalArgumentException("Missing or invalid authorization header");
            Long pharmacyId;
            // Try PHARMACIST first
            try {
                Long pharmacistId = auth.extractPharmacistIdFromToken(token);
                PharmacistUser ph = (PharmacistUser) userRepository.findById(pharmacistId)
                        .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
                if (ph.getPharmacy() == null) return ResponseEntity.badRequest().body(Map.of("error", "Pharmacist not assigned to a pharmacy"));
                pharmacyId = ph.getPharmacy().getId();
            } catch (IllegalArgumentException wrongRole) {
                // Fallback to PHARMACY_ADMIN
                Long adminId = auth.extractPharmacyAdminIdFromToken(token);
                PharmacyAdmin admin = (PharmacyAdmin) userRepository.findById(adminId)
                        .orElseThrow(() -> new IllegalArgumentException("Pharmacy admin not found"));
                if (admin.getPharmacy() == null) return ResponseEntity.badRequest().body(Map.of("error", "Pharmacy admin not assigned to a pharmacy"));
                pharmacyId = admin.getPharmacy().getId();
            }
            Wallet w = walletService.getOrCreatePharmacyWallet(pharmacyId);
            List<WalletTransactionDTO> txns = walletService.listRecentTransactionsForPharmacy(pharmacyId, pageSize).stream().map(this::toDTO).toList();
            return ResponseEntity.ok(WalletSummaryDTO.builder()
                    .ownerType("PHARMACY")
                    .ownerId(pharmacyId)
                    .currency(w.getCurrency())
                    .balance(w.getBalance())
                    .transactions(txns)
                    .build());
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
    }

    @PostMapping("/pharmacies/{pharmacyId}/payout")
    public ResponseEntity<?> payout(@PathVariable Long pharmacyId, @RequestBody Map<String, Object> body) {
        try {
            BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : null;
            String reference = body.get("reference") != null ? body.get("reference").toString() : null;
            String externalKey = body.get("externalKey") != null ? body.get("externalKey").toString() : null;
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return ResponseEntity.badRequest().body(Map.of("error", "amount > 0 required"));
            walletService.postPayout(pharmacyId, amount, reference, externalKey);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // Event ingestion endpoints
    @PostMapping("/events/customer-card-captured")
    public ResponseEntity<?> customerCardCaptured(@RequestBody CustomerCardCapturedEvent e) {
        try {
            walletService.postCustomerCardCaptured(e.getOrderCode(), e.getPharmacyOrderId(), e.getPrescriptionId(), e.getPharmacyId(), e.getAmount(), e.getPaymentId(), e.getExternalKey());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/events/customer-cash-collected")
    public ResponseEntity<?> customerCashCollected(@RequestBody CustomerCashCollectedEvent e) {
        try {
            walletService.postCustomerCashCollected(e.getOrderCode(), e.getPharmacyOrderId(), e.getPrescriptionId(), e.getPharmacyId(), e.getAmount(), e.getExternalKey());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/events/pharmacy-commission-card-captured")
    public ResponseEntity<?> pharmacyCommissionCardCaptured(@RequestBody PharmacyCommissionCardCapturedEvent e) {
        try {
            BigDecimal convenience = walletSettingsService.getSettings().getConvenienceFee();
            walletService.postCommissionCardCaptured(e.getPharmacyId(), e.getOrderCode(), e.getCommissionAmount(), convenience, e.getPaymentId(), e.getExternalKey());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/events/refund-full")
    public ResponseEntity<?> refundFull(@RequestBody RefundFullEvent e) {
        try {
            walletService.postRefundFull(e.getOrderCode(), e.getPharmacyOrderId(), e.getPharmacyId(), e.getAmount(), e.getExternalKey());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    // New pharmacy-facing GET endpoints (auth required: PHARMACIST or PHARMACY_ADMIN bound to the pharmacy)
    @GetMapping("/pharmacies/{pharmacyId}/balance")
    public ResponseEntity<?> pharmacyBalance(@PathVariable Long pharmacyId, HttpServletRequest request) {
        try {
            String token = auth.extractAndValidateToken(request);
            if (token == null) throw new IllegalArgumentException("Missing or invalid authorization header");
            Long resolvedPharmacyId;
            // Try PHARMACIST
            try {
                Long pharmacistId = auth.extractPharmacistIdFromToken(token);
                PharmacistUser ph = (PharmacistUser) userRepository.findById(pharmacistId)
                        .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
                resolvedPharmacyId = ph.getPharmacy() != null ? ph.getPharmacy().getId() : null;
            } catch (IllegalArgumentException wrongRole) {
                // Fallback to PHARMACY_ADMIN
                Long adminId = auth.extractPharmacyAdminIdFromToken(token);
                PharmacyAdmin admin = (PharmacyAdmin) userRepository.findById(adminId)
                        .orElseThrow(() -> new IllegalArgumentException("Pharmacy admin not found"));
                resolvedPharmacyId = admin.getPharmacy() != null ? admin.getPharmacy().getId() : null;
            }
            if (resolvedPharmacyId == null || !resolvedPharmacyId.equals(pharmacyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized for this pharmacy"));
            }
            Wallet w = walletService.getOrCreatePharmacyWallet(pharmacyId);
            return ResponseEntity.ok(Map.of(
                    "ownerType", "PHARMACY",
                    "ownerId", pharmacyId,
                    "walletId", w.getId(),
                    "currency", w.getCurrency(),
                    "balance", w.getBalance(),
                    "updatedAt", w.getUpdatedAt()
            ));
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
    }

    @GetMapping("/pharmacies/{pharmacyId}/transactions")
    public ResponseEntity<?> pharmacyTransactions(@PathVariable Long pharmacyId,
                                                  @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                                  @RequestParam(name = "size", required = false) Integer size,
                                                  HttpServletRequest request) {
        try {
            String token = auth.extractAndValidateToken(request);
            if (token == null) throw new IllegalArgumentException("Missing or invalid authorization header");
            Long resolvedPharmacyId;
            // Try PHARMACIST
            try {
                Long pharmacistId = auth.extractPharmacistIdFromToken(token);
                PharmacistUser ph = (PharmacistUser) userRepository.findById(pharmacistId)
                        .orElseThrow(() -> new IllegalArgumentException("Pharmacist not found"));
                resolvedPharmacyId = ph.getPharmacy() != null ? ph.getPharmacy().getId() : null;
            } catch (IllegalArgumentException wrongRole) {
                // Fallback to PHARMACY_ADMIN
                Long adminId = auth.extractPharmacyAdminIdFromToken(token);
                PharmacyAdmin admin = (PharmacyAdmin) userRepository.findById(adminId)
                        .orElseThrow(() -> new IllegalArgumentException("Pharmacy admin not found"));
                resolvedPharmacyId = admin.getPharmacy() != null ? admin.getPharmacy().getId() : null;
            }
            if (resolvedPharmacyId == null || !resolvedPharmacyId.equals(pharmacyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized for this pharmacy"));
            }
            int effectiveSize = (size != null && size > 0) ? size : pageSize;
            List<WalletTransactionDTO> txns = walletService.listRecentTransactionsForPharmacy(pharmacyId, effectiveSize)
                    .stream().map(this::toDTO).toList();
            return ResponseEntity.ok(Map.of(
                    "ownerType", "PHARMACY",
                    "ownerId", pharmacyId,
                    "pageSize", effectiveSize,
                    "transactions", txns
            ));
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
    }
}
