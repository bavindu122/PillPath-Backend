package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.order.CustomerOrderDTO;
import com.leo.pillpathbackend.dto.order.PlaceOrderRequestDTO;
import com.leo.pillpathbackend.dto.order.PayOrderRequestDTO;
import com.leo.pillpathbackend.dto.order.PharmacyOrderDTO;
import com.leo.pillpathbackend.service.OrderService;
import com.leo.pillpathbackend.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    // Customer: list own orders (optionally include items)
    @GetMapping("/my")
    public ResponseEntity<?> myOrders(@RequestParam(name = "includeItems", defaultValue = "false") boolean includeItems,
                                      HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            log.info("Customer {} listing orders includeItems={}", customerId, includeItems);
            List<CustomerOrderDTO> list = orderService.listCustomerOrders(customerId, includeItems);
            return ResponseEntity.ok(list);
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
}