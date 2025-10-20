package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.order.PharmacyOrderDTO;
import com.leo.pillpathbackend.dto.order.UpdatePharmacyOrderStatusRequest;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
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
@RequestMapping("/api/v1/orders/pharmacy")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
@Validated
public class PharmacyOrderController {

    private final OrderService orderService;
    private final AuthenticationHelper auth;

    /**
     * List pharmacy orders for authenticated pharmacist OR pharmacy admin
     * Optionally filter by status
     */
    @GetMapping
    public ResponseEntity<?> listOrders(
            @RequestParam(value = "status", required = false) PharmacyOrderStatus status,
            HttpServletRequest httpRequest) {
        try {
            // Try to extract pharmacist ID first
            Long pharmacistId = null;
            Long pharmacyAdminId = null;
            String userType = null;
            
            try {
                pharmacistId = auth.extractPharmacistIdFromRequest(httpRequest);
                userType = "PHARMACIST";
                log.info("Pharmacist {} listing pharmacy orders with status {}", pharmacistId, status);
            } catch (IllegalArgumentException e) {
                // If not a pharmacist, try pharmacy admin
                try {
                    pharmacyAdminId = auth.extractPharmacyAdminIdFromRequest(httpRequest);
                    userType = "PHARMACY_ADMIN";
                    log.info("Pharmacy Admin {} listing pharmacy orders with status {}", pharmacyAdminId, status);
                } catch (IllegalArgumentException ex) {
                    log.warn("Unauthorized access attempt to list pharmacy orders");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Unauthorized: Must be pharmacist or pharmacy admin"));
                }
            }
            
            List<PharmacyOrderDTO> list;
            
            if ("PHARMACIST".equals(userType)) {
                list = orderService.listPharmacyOrders(pharmacistId, status);
            } else {
                list = orderService.listPharmacyOrdersByAdmin(pharmacyAdminId, status);
            }
            
            return ResponseEntity.ok(list);
            
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Bad request" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error listing pharmacy orders", e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    /**
     * Get a specific pharmacy order by id
     * Accessible by both pharmacist and pharmacy admin
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
            @PathVariable("id") Long pharmacyOrderId,
            HttpServletRequest httpRequest) {
        try {
            Long pharmacistId = null;
            Long pharmacyAdminId = null;
            String userType = null;
            
            try {
                pharmacistId = auth.extractPharmacistIdFromRequest(httpRequest);
                userType = "PHARMACIST";
                log.info("Pharmacist {} fetching pharmacy order {}", pharmacistId, pharmacyOrderId);
            } catch (IllegalArgumentException e) {
                try {
                    pharmacyAdminId = auth.extractPharmacyAdminIdFromRequest(httpRequest);
                    userType = "PHARMACY_ADMIN";
                    log.info("Pharmacy Admin {} fetching pharmacy order {}", pharmacyAdminId, pharmacyOrderId);
                } catch (IllegalArgumentException ex) {
                    log.warn("Unauthorized access attempt to get pharmacy order {}", pharmacyOrderId);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Unauthorized: Must be pharmacist or pharmacy admin"));
                }
            }
            
            PharmacyOrderDTO dto;
            
            if ("PHARMACIST".equals(userType)) {
                dto = orderService.getPharmacyOrder(pharmacistId, pharmacyOrderId);
            } else {
                dto = orderService.getPharmacyOrderByAdmin(pharmacyAdminId, pharmacyOrderId);
            }
            
            return ResponseEntity.ok(dto);
            
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Not found" : e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error fetching pharmacy order {}", pharmacyOrderId, e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }

    /**
     * Update pharmacy order status
     * Accessible by both pharmacist and pharmacy admin
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") Long pharmacyOrderId,
            @Valid @RequestBody UpdatePharmacyOrderStatusRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request == null || request.getStatus() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "status required"));
            }
            
            Long pharmacistId = null;
            Long pharmacyAdminId = null;
            String userType = null;
            
            try {
                pharmacistId = auth.extractPharmacistIdFromRequest(httpRequest);
                userType = "PHARMACIST";
                log.info("Pharmacist {} updating order {} to status {}", pharmacistId, pharmacyOrderId, request.getStatus());
            } catch (IllegalArgumentException e) {
                try {
                    pharmacyAdminId = auth.extractPharmacyAdminIdFromRequest(httpRequest);
                    userType = "PHARMACY_ADMIN";
                    log.info("Pharmacy Admin {} updating order {} to status {}", pharmacyAdminId, pharmacyOrderId, request.getStatus());
                } catch (IllegalArgumentException ex) {
                    log.warn("Unauthorized access attempt to update pharmacy order {}", pharmacyOrderId);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Unauthorized: Must be pharmacist or pharmacy admin"));
                }
            }
            
            PharmacyOrderDTO dto;
            
            if ("PHARMACIST".equals(userType)) {
                dto = orderService.updatePharmacyOrderStatus(pharmacistId, pharmacyOrderId, request.getStatus());
            } else {
                dto = orderService.updatePharmacyOrderStatusByAdmin(pharmacyAdminId, pharmacyOrderId, request.getStatus());
            }
            
            return ResponseEntity.ok(dto);
            
        } catch (IllegalArgumentException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Bad request" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        } catch (IllegalStateException e) {
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Invalid status transition" : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
        } catch (Exception e) {
            log.error("Unexpected error updating status for pharmacy order {}", pharmacyOrderId, e);
            String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
        }
    }
}





























// package com.leo.pillpathbackend.controller;

// import com.leo.pillpathbackend.dto.order.PharmacyOrderDTO;
// import com.leo.pillpathbackend.dto.order.UpdatePharmacyOrderStatusRequest;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
// import com.leo.pillpathbackend.service.OrderService;
// import com.leo.pillpathbackend.util.AuthenticationHelper;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.*;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.validation.Valid;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/v1/orders/pharmacy")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// @Slf4j
// @Validated
// public class PharmacyOrderController {

//     private final OrderService orderService;
//     private final AuthenticationHelper auth;

//     // List pharmacy orders for the authenticated pharmacist (optionally filter by status)
//     @GetMapping
//     public ResponseEntity<?> listOrders(@RequestParam(value = "status", required = false) PharmacyOrderStatus status,
//                                         HttpServletRequest httpRequest) {
//         try {
//             Long pharmacistId = auth.extractPharmacistIdFromRequest(httpRequest);
//             log.info("Pharmacist {} listing pharmacy orders with status {}", pharmacistId, status);
//             List<PharmacyOrderDTO> list = orderService.listPharmacyOrders(pharmacistId, status);
//             return ResponseEntity.ok(list);
//         } catch (IllegalArgumentException e) {
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Bad request" : e.getMessage();
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
//         } catch (Exception e) {
//             log.error("Unexpected error listing pharmacy orders", e);
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
//         }
//     }

//     // Get a specific pharmacy order by id
//     @GetMapping("/{id}")
//     public ResponseEntity<?> getOrder(@PathVariable("id") Long pharmacyOrderId, HttpServletRequest httpRequest) {
//         try {
//             Long pharmacistId = auth.extractPharmacistIdFromRequest(httpRequest);
//             log.info("Pharmacist {} fetching pharmacy order {}", pharmacistId, pharmacyOrderId);
//             PharmacyOrderDTO dto = orderService.getPharmacyOrder(pharmacistId, pharmacyOrderId);
//             return ResponseEntity.ok(dto);
//         } catch (IllegalArgumentException e) {
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Not found" : e.getMessage();
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
//         } catch (Exception e) {
//             log.error("Unexpected error fetching pharmacy order {}", pharmacyOrderId, e);
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
//         }
//     }

//     // Update pharmacy order status
//     @PatchMapping("/{id}/status")
//     public ResponseEntity<?> updateStatus(@PathVariable("id") Long pharmacyOrderId,
//                                           @Valid @RequestBody UpdatePharmacyOrderStatusRequest request,
//                                           HttpServletRequest httpRequest) {
//         try {
//             Long pharmacistId = auth.extractPharmacistIdFromRequest(httpRequest);
//             log.info("Pharmacist {} updating order {} to status {}", pharmacistId, pharmacyOrderId, request != null ? request.getStatus() : null);
//             if (request == null || request.getStatus() == null) {
//                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "status required"));
//             }
//             PharmacyOrderDTO dto = orderService.updatePharmacyOrderStatus(pharmacistId, pharmacyOrderId, request.getStatus());
//             return ResponseEntity.ok(dto);
//         } catch (IllegalArgumentException e) {
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Bad request" : e.getMessage();
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
//         } catch (IllegalStateException e) {
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? "Invalid status transition" : e.getMessage();
//             return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", msg));
//         } catch (Exception e) {
//             log.error("Unexpected error updating status for pharmacy order {}", pharmacyOrderId, e);
//             String msg = (e.getMessage() == null || e.getMessage().isBlank()) ? e.getClass().getSimpleName() : e.getMessage();
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
//         }
//     }
// }
