package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.otcorder.OrderRequestDTO;
import com.leo.pillpathbackend.dto.otcorder.OrderResponseDTO;
import com.leo.pillpathbackend.service.OtcOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/otc-orders")
public class OtcOrderController {

    public OtcOrderController(OtcOrderService otcOrderService) {
        this.otcOrderService = otcOrderService;
        System.out.println("🟢🟢🟢 OtcOrderController LOADED! 🟢🟢🟢");
        System.out.println("🟢 Mapped to: /api/otc-orders");
    }

    private final OtcOrderService otcOrderService;

    // ✅ TEST ENDPOINT - Check if controller is accessible
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("🟢 TEST endpoint hit - Controller is working!");
        return ResponseEntity.ok("OTC Orders endpoint is working!");
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        System.out.println("🔵 POST /api/otc-orders HIT!");
        System.out.println("🔵 Request received: " + orderRequest);
        System.out.println("🔵 Customer ID: " + orderRequest.getCustomerId());
        System.out.println("🔵 Payment Method: " + orderRequest.getPaymentMethod());
        System.out.println("🔵 Items count: " + (orderRequest.getItems() != null ? orderRequest.getItems().size() : 0));
        
        try {
            OrderResponseDTO response = otcOrderService.createOrder(orderRequest);
            System.out.println("✅ Order created successfully!");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            System.err.println("❌ IllegalStateException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long orderId) {
        try {
            OrderResponseDTO response = otcOrderService.getOrderById(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<OrderResponseDTO> getOrderByCode(@PathVariable String orderCode) {
        try {
            OrderResponseDTO response = otcOrderService.getOrderByCode(orderCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getCustomerOrders(@PathVariable Long customerId) {
        try {
            List<OrderResponseDTO> orders = otcOrderService.getCustomerOrders(customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            OrderResponseDTO response = otcOrderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}