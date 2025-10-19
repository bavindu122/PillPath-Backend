package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.finance.*;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.*;
import com.leo.pillpathbackend.repository.*;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.WalletSettingsService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminFinanceController {

    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final CommissionRecordRepository commissionRecordRepository;
    private final PayoutRecordRepository payoutRecordRepository;
    private final WalletSettingsService walletSettingsService;
    private final CloudinaryService cloudinaryService;
    private final AuthenticationHelper auth;

    private static SettlementChannel resolveChannel(PaymentMethod method) {
        if (method == null) return SettlementChannel.ON_HAND; // safest default
        return method == PaymentMethod.CASH ? SettlementChannel.ON_HAND : SettlementChannel.ONLINE;
    }

    private static Boolean mapOnHandReceived(CommissionStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PAID -> Boolean.TRUE;
            case UNPAID -> Boolean.FALSE;
            case NOT_PAID -> null;
        };
    }

    // GET /admin/order-payments
    @GetMapping("/order-payments")
    public ResponseEntity<?> listOrderPayments(
            @RequestParam(value = "pharmacyId", required = false) Long pharmacyId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "settlementChannel", required = false) SettlementChannel channel,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        try {
            auth.extractAdminIdFromRequest(request);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        int p = Math.max(page, 1) - 1;
        int s = Math.max(size, 1);
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "updatedAt"));

        // Compute optional date range [start,end)
        final LocalDateTime startTs;
        final LocalDateTime endTs;
        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, Math.max(1, Math.min(12, month)));
            startTs = ym.atDay(1).atStartOfDay();
            endTs = ym.plusMonths(1).atDay(1).atStartOfDay();
        } else if (year != null) {
            startTs = LocalDate.of(year, 1, 1).atStartOfDay();
            endTs = LocalDate.of(year + 1, 1, 1).atStartOfDay();
        } else {
            startTs = null;
            endTs = null;
        }

        Specification<PharmacyOrder> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            // join
            Join<PharmacyOrder, Pharmacy> jPharmacy = root.join("pharmacy");
            Join<PharmacyOrder, CustomerOrder> jOrder = root.join("customerOrder");
            Join<CustomerOrder, Customer> jCustomer = jOrder.join("customer");

            // completed or cancelled slices are most relevant
            preds.add(root.get("status").in(PharmacyOrderStatus.HANDED_OVER, PharmacyOrderStatus.CANCELLED));

            if (pharmacyId != null) {
                preds.add(cb.equal(jPharmacy.get("id"), pharmacyId));
            }
            if (startTs != null && endTs != null) {
                preds.add(cb.between(root.get("updatedAt"), startTs, endTs));
            }
            if (channel != null) {
                // map channel from payment method
                if (channel == SettlementChannel.ON_HAND) {
                    preds.add(cb.equal(jOrder.get("paymentMethod"), PaymentMethod.CASH));
                } else {
                    preds.add(cb.notEqual(jOrder.get("paymentMethod"), PaymentMethod.CASH));
                }
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                Predicate byId = cb.like(cb.lower(root.get("id").as(String.class)), like);
                Predicate byCode = cb.like(cb.lower(root.get("orderCode")), like);
                Predicate byCustomer = cb.like(cb.lower(jCustomer.get("fullName")), like);
                Predicate byPharmacy = cb.like(cb.lower(jPharmacy.get("name")), like);
                preds.add(cb.or(byId, byCode, byCustomer, byPharmacy));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        Page<PharmacyOrder> poPage = pharmacyOrderRepository.findAll(spec, pageable);
        List<OrderPaymentDTO> items = new ArrayList<>();
        for (PharmacyOrder po : poPage.getContent()) {
            CustomerOrder co = po.getCustomerOrder();
            Pharmacy ph = po.getPharmacy();
            PaymentMethod pm = co != null ? co.getPaymentMethod() : null;
            SettlementChannel sc = resolveChannel(pm);
            Long pid = ph != null ? ph.getId() : null;
            BigDecimal gross = Optional.ofNullable(po.getTotal()).orElse(BigDecimal.ZERO);
            BigDecimal rate = walletSettingsService.resolveCommissionPercent(pid);
            BigDecimal commission = gross.multiply(rate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal net = sc == SettlementChannel.ONLINE ? gross.subtract(commission) : BigDecimal.ZERO;

            String commissionId = null; Boolean received = null; String payoutId = null;
            if (sc == SettlementChannel.ON_HAND) {
                Optional<CommissionRecord> crOpt = commissionRecordRepository.findByOrderId(po.getId());
                if (crOpt.isPresent()) {
                    CommissionRecord cr = crOpt.get();
                    commissionId = String.valueOf(cr.getId());
                    received = mapOnHandReceived(cr.getStatus());
                } else {
                    received = Boolean.FALSE; // default
                }
            } else {
                Optional<PayoutRecord> prOpt = payoutRecordRepository.findByOrderId(po.getId());
                payoutId = prOpt.map(pr -> String.valueOf(pr.getId())).orElse(null);
            }

            items.add(OrderPaymentDTO.builder()
                    .id(String.valueOf(po.getId()))
                    .orderCode(po.getOrderCode())
                    .date(po.getUpdatedAt() != null ? po.getUpdatedAt().toString() : null)
                    .customerName(co != null && co.getCustomer() != null ? co.getCustomer().getFullName() : null)
                    .pharmacyId(pid != null ? String.valueOf(pid) : null)
                    .pharmacyName(ph != null ? ph.getName() : null)
                    .settlementChannel(sc)
                    .paymentMethod(pm)
                    .grossAmount(gross)
                    .commissionRate(rate)
                    .commissionAmount(commission)
                    .netPayoutAmount(net)
                    .onHandCommissionReceived(sc == SettlementChannel.ON_HAND ? received : null)
                    .commissionId(commissionId)
                    .payoutId(payoutId)
                    .build());
        }

        PagedResponse<OrderPaymentDTO> resp = PagedResponse.<OrderPaymentDTO>builder()
                .items(items)
                .page(p + 1)
                .size(s)
                .total(poPage.getTotalElements())
                .build();
        return ResponseEntity.ok(resp);
    }

    // GET /admin/commissions
    @GetMapping("/commissions")
    public ResponseEntity<?> listCommissions(
            @RequestParam(value = "pharmacyId", required = false) Long pharmacyId,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "status", required = false) CommissionStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        try {
            auth.extractAdminIdFromRequest(request);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        int p = Math.max(page, 1) - 1;
        int s = Math.max(size, 1);
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Specification<CommissionRecord> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (pharmacyId != null) preds.add(cb.equal(root.get("pharmacyId"), pharmacyId));
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (month != null && !month.isBlank()) preds.add(cb.equal(root.get("month"), month));
            if (year != null && !year.isBlank()) preds.add(cb.like(root.get("month"), "%/" + year));
            return cb.and(preds.toArray(new Predicate[0]));
        };
        Page<CommissionRecord> pageData = commissionRecordRepository.findAll(spec, pageable);
        List<CommissionDTO> items = pageData.getContent().stream().map(this::toCommissionDTO).toList();
        return ResponseEntity.ok(PagedResponse.<CommissionDTO>builder()
                .items(items).page(p + 1).size(s).total(pageData.getTotalElements()).build());
    }

    private CommissionDTO toCommissionDTO(CommissionRecord cr) {
        return CommissionDTO.builder()
                .id(String.valueOf(cr.getId()))
                .orderId(cr.getOrderId() != null ? String.valueOf(cr.getOrderId()) : null)
                .orderCode(cr.getOrderCode())
                .pharmacyId(cr.getPharmacyId() != null ? String.valueOf(cr.getPharmacyId()) : null)
                .pharmacyName(cr.getPharmacyName())
                .amount(cr.getAmount())
                .month(cr.getMonth())
                .status(cr.getStatus())
                .paidAt(cr.getPaidAt() != null ? cr.getPaidAt().toString() : null)
                .note(cr.getNote())
                .createdAt(cr.getCreatedAt() != null ? cr.getCreatedAt().toString() : null)
                .updatedAt(cr.getUpdatedAt() != null ? cr.getUpdatedAt().toString() : null)
                .build();
    }

    @PatchMapping("/commissions/{commissionId}")
    public ResponseEntity<?> updateCommission(@PathVariable Long commissionId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            auth.extractAdminIdFromRequest(request);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        Optional<CommissionRecord> opt = commissionRecordRepository.findById(commissionId);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Commission not found"));
        CommissionRecord cr = opt.get();
        String statusStr = body.get("status") != null ? body.get("status").toString() : null;
        String note = body.get("note") != null ? body.get("note").toString() : null;
        if (statusStr == null) return ResponseEntity.badRequest().body(Map.of("error", "status required"));
        CommissionStatus status;
        try { status = CommissionStatus.valueOf(statusStr); } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
        cr.setStatus(status);
        cr.setNote(note);
        if (status == CommissionStatus.PAID) {
            cr.setPaidAt(LocalDateTime.now(ZoneId.of("UTC")));
        } else {
            cr.setPaidAt(null);
        }
        commissionRecordRepository.save(cr);
        return ResponseEntity.ok(toCommissionDTO(cr));
    }

    // GET /admin/payouts
    @GetMapping("/payouts")
    public ResponseEntity<?> listPayouts(
            @RequestParam(value = "pharmacyId", required = false) Long pharmacyId,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "status", required = false) PayoutStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        try {
            auth.extractAdminIdFromRequest(request);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        int p = Math.max(page, 1) - 1;
        int s = Math.max(size, 1);
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Specification<PayoutRecord> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (pharmacyId != null) preds.add(cb.equal(root.get("pharmacyId"), pharmacyId));
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (month != null && !month.isBlank()) preds.add(cb.equal(root.get("month"), month));
            if (year != null && !year.isBlank()) preds.add(cb.like(root.get("month"), "%/" + year));
            return cb.and(preds.toArray(new Predicate[0]));
        };
        Page<PayoutRecord> pageData = payoutRecordRepository.findAll(spec, pageable);
        List<PayoutDTO> items = pageData.getContent().stream().map(this::toPayoutDTO).toList();
        return ResponseEntity.ok(PagedResponse.<PayoutDTO>builder()
                .items(items).page(p + 1).size(s).total(pageData.getTotalElements()).build());
    }

    private PayoutDTO toPayoutDTO(PayoutRecord pr) {
        return PayoutDTO.builder()
                .id(String.valueOf(pr.getId()))
                .orderId(pr.getOrderId() != null ? String.valueOf(pr.getOrderId()) : null)
                .orderCode(pr.getOrderCode())
                .pharmacyId(pr.getPharmacyId() != null ? String.valueOf(pr.getPharmacyId()) : null)
                .pharmacyName(pr.getPharmacyName())
                .amount(pr.getAmount())
                .month(pr.getMonth())
                .status(pr.getStatus())
                .paidAt(pr.getPaidAt() != null ? pr.getPaidAt().toString() : null)
                .receiptUrl(pr.getReceiptUrl())
                .receiptFileName(pr.getReceiptFileName())
                .receiptFileType(pr.getReceiptFileType())
                .note(pr.getNote())
                .createdAt(pr.getCreatedAt() != null ? pr.getCreatedAt().toString() : null)
                .updatedAt(pr.getUpdatedAt() != null ? pr.getUpdatedAt().toString() : null)
                .build();
    }

    // POST /admin/uploads/payout-receipts
    @PostMapping(value = "/uploads/payout-receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPayoutReceipt(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            auth.extractAdminIdFromRequest(request);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        try {
            Map<String, Object> res = cloudinaryService.uploadPayoutReceipt(file);
            String url = (String) Optional.ofNullable(res.get("secure_url")).orElse(res.get("url"));
            String format = (String) res.get("format");
            String original = (String) res.get("original_filename");
            return ResponseEntity.ok(FileUploadResult.builder()
                    .url(url)
                    .fileName((original != null ? original : "receipt") + (format != null ? ("." + format) : ""))
                    .fileType(file.getContentType())
                    .size(file.getSize())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /admin/payouts/{payoutId}
    @PatchMapping("/payouts/{payoutId}")
    public ResponseEntity<?> updatePayout(@PathVariable Long payoutId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            auth.extractAdminIdFromRequest(request);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        Optional<PayoutRecord> opt = payoutRecordRepository.findById(payoutId);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Payout not found"));
        PayoutRecord pr = opt.get();
        String statusStr = body.get("status") != null ? body.get("status").toString() : null;
        if (statusStr != null) {
            try {
                pr.setStatus(PayoutStatus.valueOf(statusStr));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
            }
        }
        if (body.get("receiptUrl") != null) pr.setReceiptUrl(body.get("receiptUrl").toString());
        if (body.get("receiptFileName") != null) pr.setReceiptFileName(body.get("receiptFileName").toString());
        if (body.get("receiptFileType") != null) pr.setReceiptFileType(body.get("receiptFileType").toString());
        if (body.get("paidAt") != null) {
            try {
                pr.setPaidAt(LocalDateTime.parse(body.get("paidAt").toString()));
            } catch (Exception ignored) { /* ignore parse errors */ }
        }
        if (body.get("note") != null) pr.setNote(body.get("note").toString());

        payoutRecordRepository.save(pr);
        return ResponseEntity.ok(toPayoutDTO(pr));
    }
}
