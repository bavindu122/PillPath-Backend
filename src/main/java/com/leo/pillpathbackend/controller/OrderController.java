package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.order.CustomerOrderDTO;
import com.leo.pillpathbackend.dto.order.PlaceOrderRequestDTO;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error placing order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // Customer fetches own order by public code
    @GetMapping("/{orderCode}")
    public ResponseEntity<?> getOrder(@PathVariable String orderCode, HttpServletRequest httpRequest) {
        try {
            Long customerId = auth.extractCustomerIdFromRequest(httpRequest);
            log.info("Customer {} fetching order {}", customerId, orderCode);
            CustomerOrderDTO dto = orderService.getCustomerOrder(customerId, orderCode);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching order {}", orderCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}

