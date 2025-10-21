package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.order.CustomerOrderDTO;
import com.leo.pillpathbackend.dto.order.PlaceOrderRequestDTO;
import com.leo.pillpathbackend.dto.order.PayOrderRequestDTO;
import com.leo.pillpathbackend.dto.order.PharmacyOrderDTO;
import com.leo.pillpathbackend.dto.order.UnifiedOrderSummaryDTO;
import com.leo.pillpathbackend.dto.order.OrderTotalsDTO;
import com.leo.pillpathbackend.dto.order.PaymentDTO;
import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import com.leo.pillpathbackend.entity.enums.PaymentStatus;
import com.leo.pillpathbackend.dto.PharmacyReviewRequest;
import com.leo.pillpathbackend.dto.PharmacyReviewResponse;
import com.leo.pillpathbackend.service.OrderService;
import com.leo.pillpathbackend.service.OtcOrderService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import com.leo.pillpathbackend.repository.OtcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
@Validated
public class OrderController {

    private final OrderService orderService;
    private final AuthenticationHelper auth;
    private final OtcOrderService otcOrderService;
    private final OtcRepository otcRepository;

    // Customer places multi-pharmacy order
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO request, HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            log.info("Customer {} placing order for prescriptionCode={} pharmacies={} ", customerId, request.getPrescriptionCode(),
                    request.getPharmacies() != null ? request.getPharmacies().size() : 0);
            CustomerOrderDTO dto = orderService.placeOrder(customerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Order placement validation error: {}", e.getMessage());
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Bad request" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        } catch (IllegalStateException e) {
            log.warn("Order placement state error: {}", e.getMessage());
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Invalid order state" : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error placing order", e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    // Customer: list own orders (optionally include items) with type filter (all|prescription|otc)
    @GetMapping("/my")
    public ResponseEntity<?> myOrders(@RequestParam(name = "includeItems", defaultValue = "false") boolean includeItems,
                                      @RequestParam(name = "type", defaultValue = "all") String type,
                                      HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            log.info("Customer {} listing orders includeItems={} type={}", customerId, includeItems, type);

            List<UnifiedOrderSummaryDTO> result = new ArrayList<>();

            boolean wantPresc = "all".equalsIgnoreCase(type) || "prescription".equalsIgnoreCase(type);
            boolean wantOtc = "all".equalsIgnoreCase(type) || "otc".equalsIgnoreCase(type);

            if (wantPresc) {
                List<CustomerOrderDTO> presc = orderService.listCustomerOrders(customerId, includeItems);
                // Only keep orders that are true prescriptions (avoid OTC duplicates)
                presc.stream()
                        .filter(co -> co.getPrescriptionId() != null)
                        .forEach(co -> {
                            UnifiedOrderSummaryDTO.UnifiedOrderSummaryDTOBuilder b = UnifiedOrderSummaryDTO.builder()
                                    .orderType("PRESCRIPTION")
                                    .orderCode(co.getOrderCode())
                                    .createdAt(co.getCreatedAt())
                                    .status(co.getStatus() != null ? co.getStatus().name() : null)
                                    .payment(co.getPayment())
                                    .totals(co.getTotals());

                            if (co.getPharmacyOrders() != null) {
                                List<UnifiedOrderSummaryDTO.UnifiedPharmacyOrderSummaryDTO> pos = co.getPharmacyOrders().stream().map(po ->
                                        UnifiedOrderSummaryDTO.UnifiedPharmacyOrderSummaryDTO.builder()
                                                .orderCode(po.getOrderCode())
                                                .pharmacyId(po.getPharmacyId())
                                                .pharmacyName(po.getPharmacyName())
                                                .status(po.getStatus() != null ? po.getStatus().name() : null)
                                                .prescriptionImageUrl(po.getPrescriptionImageUrl())
                                                .build()
                                ).toList();
                                b.pharmacyOrders(pos);
                            }

                            result.add(b.build());
                        });
            }

            if (wantOtc) {
                var otcOrders = otcOrderService.getCustomerOrders(customerId);
                for (var oo : otcOrders) {
                    OrderTotalsDTO totals = OrderTotalsDTO.builder()
                            .total(oo.getTotal())
                            .currency("LKR")
                            .build();

                    PaymentDTO payment;
                    try {
                        PaymentMethod pm = oo.getPaymentMethod() != null ? PaymentMethod.valueOf(oo.getPaymentMethod()) : null;
                        PaymentStatus ps = oo.getPaymentStatus() != null ? PaymentStatus.valueOf(oo.getPaymentStatus()) : null;
                        payment = PaymentDTO.builder()
                                .method(pm)
                                .status(ps)
                                .amount(oo.getTotal())
                                .currency("LKR")
                                .reference(null)
                                .build();
                    } catch (Exception ignore) {
                        payment = PaymentDTO.builder()
                                .method(null)
                                .status(null)
                                .amount(oo.getTotal())
                                .currency("LKR")
                                .reference(null)
                                .build();
                    }

                    UnifiedOrderSummaryDTO.UnifiedOrderSummaryDTOBuilder b = UnifiedOrderSummaryDTO.builder()
                            .orderType("OTC")
                            .orderCode(oo.getOrderCode())
                            .createdAt(oo.getCreatedAt() != null ? oo.getCreatedAt().toString() : null)
                            .status(oo.getStatus())
                            .payment(payment)
                            .totals(totals);

                    if (oo.getPharmacyOrders() != null && !oo.getPharmacyOrders().isEmpty()) {
                        var first = oo.getPharmacyOrders().get(0);
                        b.pharmacyId(first.getPharmacyId());
                        b.pharmacyName(first.getPharmacyName());

                        if (includeItems && first.getItems() != null) {
                            // Enrich items with productId and imageUrl by matching OTC products in the same pharmacy by name
                            var otcProducts = otcRepository.findByPharmacyId(first.getPharmacyId());
                            var items = first.getItems().stream().map(it -> {
                                var productMatch = otcProducts.stream()
                                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(it.getMedicineName()))
                                        .findFirst()
                                        .orElse(null);
                                return UnifiedOrderSummaryDTO.UnifiedOtcItemDTO.builder()
                                        .otcProductId(productMatch != null ? productMatch.getId() : null)
                                        .name(it.getMedicineName())
                                        .quantity(it.getQuantity())
                                        .unitPrice(it.getUnitPrice())
                                        .productImageUrl(productMatch != null ? productMatch.getImageUrl() : null)
                                        .build();
                            }).toList();
                            b.items(items);
                        }
                    }

                    result.add(b.build());
                }
            }

            // Sort by createdAt descending (most recent first)
            result.sort(Comparator.comparing(
                    UnifiedOrderSummaryDTO::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Unauthorized" : e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error listing customer orders", e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    // Customer fetches own order by public code; also supports pharmacy order codes
    @GetMapping("/{orderCode}")
    public ResponseEntity<?> getOrder(@PathVariable String orderCode, HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            log.info("Customer {} fetching order {}", customerId, orderCode);
            try {
                CustomerOrderDTO dto = orderService.getCustomerOrder(customerId, orderCode);
                return ResponseEntity.ok(dto);
            } catch (IllegalArgumentException nf) {
                // Try pharmacy order code fallback
                PharmacyOrderDTO pDto = orderService.getCustomerPharmacyOrderByCode(customerId, orderCode);
                return ResponseEntity.ok(pDto);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching order {}", orderCode, e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    // Explicit: fetch pharmacy order by code
    @GetMapping("/pharmacy/code/{code}")
    public ResponseEntity<?> getPharmacyOrderByCode(@PathVariable("code") String code, HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            PharmacyOrderDTO dto = orderService.getCustomerPharmacyOrderByCode(customerId, code);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching pharmacy order by code {}", code, e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    // Customer pays for an order
    @PostMapping("/{orderCode}/pay")
    public ResponseEntity<?> payOrder(@PathVariable String orderCode, @RequestBody(required = false) PayOrderRequestDTO request, HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            log.info("Customer {} paying order {}", customerId, orderCode);
            CustomerOrderDTO dto = orderService.payOrder(customerId, orderCode, request);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error paying order {}", orderCode, e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    // Customer: assign order to a family member
    @PutMapping("/{orderCode}/assign-family-member")
    public ResponseEntity<?> assignOrderToFamilyMember(
            @PathVariable String orderCode,
            @RequestBody Map<String, Long> body,
            HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            Long familyMemberId = body.get("familyMemberId");
            
            if (familyMemberId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "familyMemberId is required"));
            }
            
            log.info("Customer {} assigning order {} to family member {}", customerId, orderCode, familyMemberId);
            orderService.assignOrderToFamilyMember(orderCode, customerId, familyMemberId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Order assigned successfully",
                "orderCode", orderCode,
                "familyMemberId", familyMemberId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error assigning order {} to family member", orderCode, e);

            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }
    // Customer submits a review for a pharmacy in an order
    @PostMapping("/{orderCode}/pharmacies/{pharmacyId}/reviews")
    public ResponseEntity<?> submitPharmacyReview(@PathVariable String orderCode,
                                                  @PathVariable Long pharmacyId,
                                                  @Valid @RequestBody PharmacyReviewRequest request,
                                                  HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            PharmacyReviewResponse resp = orderService.submitPharmacyReview(customerId, orderCode, pharmacyId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Bad request" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        } catch (IllegalStateException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Conflict" : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error submitting review for order {} pharmacy {}", orderCode, pharmacyId, e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Internal server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }
}
