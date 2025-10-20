package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.otcorder.*;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import com.leo.pillpathbackend.entity.enums.PaymentStatus;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.repository.*;
import com.leo.pillpathbackend.service.OtcOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtcOrderServiceImpl implements OtcOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    // Removed unused: private final PharmacyOrderRepository pharmacyOrderRepository;
    private final CustomerRepository customerRepository;
    private final PharmacyRepository pharmacyRepository;
    private final OtcRepository otcRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        log.info("Creating OTC order for customer: {}", orderRequest.getCustomerId());

        // Validate customer
        Customer customer = customerRepository.findById(orderRequest.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        // Generate order code
        String orderCode = generateOrderCode();

        // Group items by pharmacy
        Map<Long, List<OrderItemDTO>> itemsByPharmacy = orderRequest.getItems().stream()
                .collect(Collectors.groupingBy(OrderItemDTO::getPharmacyId));

        log.info("Order contains items from {} pharmacies", itemsByPharmacy.size());

        // Create customer order (NO PRESCRIPTION - this is OTC)
        CustomerOrder customerOrder = CustomerOrder.builder()
                .orderCode(orderCode)
                .customer(customer)
                .prescription(null)  // ✅ NULL for OTC orders
                .status(CustomerOrderStatus.PENDING)
                .paymentMethod(orderRequest.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .notes(orderRequest.getNotes())
                .subtotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shipping(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .currency("LKR")
                .pharmacyOrders(new ArrayList<>())
                .build();

        BigDecimal grandTotal = BigDecimal.ZERO;

        // Create pharmacy orders
        for (Map.Entry<Long, List<OrderItemDTO>> entry : itemsByPharmacy.entrySet()) {
            Long pharmacyId = entry.getKey();
            List<OrderItemDTO> pharmacyItems = entry.getValue();

            Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                    .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found: " + pharmacyId));

            PharmacyOrder pharmacyOrder = PharmacyOrder.builder()
                    .orderCode(generatePharmacyOrderCode(orderCode, pharmacyId))
                    .customerOrder(customerOrder)
                    .pharmacy(pharmacy)
                    .submission(null)  // ✅ NULL for OTC orders (no prescription submission)
                    .status(PharmacyOrderStatus.RECEIVED)
                    .pickupCode(generatePickupCode(pharmacyId))
                    .pickupLocation(pharmacy.getAddress())
                    .customerNote(orderRequest.getNotes())
                    .subtotal(BigDecimal.ZERO)
                    .discount(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO)
                    .shipping(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .items(new ArrayList<>())
                    .build();

            BigDecimal pharmacySubtotal = BigDecimal.ZERO;

            // Create order items
            for (OrderItemDTO itemDTO : pharmacyItems) {
                Otc otcProduct = otcRepository.findById(itemDTO.getOtcProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDTO.getOtcProductId()));

                // Validate stock
                if (otcProduct.getStock() < itemDTO.getQuantity()) {
                    throw new IllegalStateException(
                        String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            otcProduct.getName(), otcProduct.getStock(), itemDTO.getQuantity())
                    );
                }

                BigDecimal unitPrice = BigDecimal.valueOf(otcProduct.getPrice());
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                PharmacyOrderItem orderItem = PharmacyOrderItem.builder()
                        .pharmacyOrder(pharmacyOrder)
                        .submissionItem(null)  // ✅ NULL for OTC orders
                        .medicineName(otcProduct.getName())
                        .genericName(otcProduct.getName())
                        .dosage(otcProduct.getDosage())
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(unitPrice)
                        .totalPrice(totalPrice)
                        .build();

                pharmacyOrder.getItems().add(orderItem);
                pharmacySubtotal = pharmacySubtotal.add(totalPrice);

                // Update stock
                otcProduct.setStock(otcProduct.getStock() - itemDTO.getQuantity());
                otcRepository.save(otcProduct);

                log.info("Added: {} x{} = Rs.{}", otcProduct.getName(), itemDTO.getQuantity(), totalPrice);
            }

            pharmacyOrder.setSubtotal(pharmacySubtotal);
            pharmacyOrder.setTotal(pharmacySubtotal);
            customerOrder.getPharmacyOrders().add(pharmacyOrder);
            grandTotal = grandTotal.add(pharmacySubtotal);
        }

        customerOrder.setSubtotal(grandTotal);
        customerOrder.setTotal(grandTotal);

        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
        log.info("OTC Order created: {}, Total: Rs.{}", orderCode, grandTotal);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Only return OTC orders (no prescription)
        if (order.getPrescription() != null) {
            throw new EntityNotFoundException("This is not an OTC order");
        }
        
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByCode(String orderCode) {
        CustomerOrder order = customerOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Only return OTC orders (no prescription)
        if (order.getPrescription() != null) {
            throw new EntityNotFoundException("This is not an OTC order");
        }
        
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getCustomerOrders(Long customerId) {
        List<CustomerOrder> orders = customerOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        
        // Filter only OTC orders (no prescription)
        return orders.stream()
                .filter(order -> order.getPrescription() == null)
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // Only update OTC orders
        if (order.getPrescription() != null) {
            throw new IllegalStateException("Cannot update prescription order through OTC endpoint");
        }
        
        order.setStatus(CustomerOrderStatus.valueOf(status));
        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        
        log.info("OTC Order {} status updated to {}", orderId, status);
        return mapToOrderResponse(updatedOrder);
    }

    // ==================== HELPER METHODS ====================

    private String generateOrderCode() {
        return "OTC-" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    private String generatePharmacyOrderCode(String orderCode, Long pharmacyId) {
        return orderCode + "-P" + pharmacyId;
    }

    private String generatePickupCode(Long pharmacyId) {
        return "PU-OTC-" + pharmacyId + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private OrderResponseDTO mapToOrderResponse(CustomerOrder order) {
        List<PharmacyOrderDTO> pharmacyOrderDTOs = order.getPharmacyOrders().stream()
                .map(this::mapToPharmacyOrderDTO)
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paymentStatus(order.getPaymentStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .createdAt(order.getCreatedAt())
                .pharmacyOrders(pharmacyOrderDTOs)
                .build();
    }

    private PharmacyOrderDTO mapToPharmacyOrderDTO(PharmacyOrder pharmacyOrder) {
        List<OrderItemDetailDTO> itemDTOs = pharmacyOrder.getItems().stream()
                .map(this::mapToOrderItemDetailDTO)
                .collect(Collectors.toList());

        return PharmacyOrderDTO.builder()
                .pharmacyOrderId(pharmacyOrder.getId())
                .orderCode(pharmacyOrder.getOrderCode())
                .pharmacyId(pharmacyOrder.getPharmacy().getId())
                .pharmacyName(pharmacyOrder.getPharmacy().getName())
                .pharmacyAddress(pharmacyOrder.getPharmacy().getAddress())
                .pharmacyPhone(pharmacyOrder.getPharmacy().getPhoneNumber())
                .subtotal(pharmacyOrder.getSubtotal())
                .status(pharmacyOrder.getStatus().name())
                .items(itemDTOs)
                .build();
    }

    private OrderItemDetailDTO mapToOrderItemDetailDTO(PharmacyOrderItem item) {
        return OrderItemDetailDTO.builder()
                .itemId(item.getId())
                .medicineName(item.getMedicineName())
                .genericName(item.getGenericName())
                .dosage(item.getDosage())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}





























// package com.leo.pillpathbackend.service.impl;

// import com.leo.pillpathbackend.dto.otcorder.*;
// import com.leo.pillpathbackend.entity.*;
// import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
// import com.leo.pillpathbackend.entity.enums.PaymentStatus;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
// import com.leo.pillpathbackend.repository.*;
// import com.leo.pillpathbackend.service.OtcOrderService;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class OtcOrderServiceImpl implements OtcOrderService {

//     private final CustomerOrderRepository customerOrderRepository;
//     private final PharmacyOrderRepository pharmacyOrderRepository;
//     private final CustomerRepository customerRepository;
//     private final PharmacyRepository pharmacyRepository;
//     private final OtcRepository otcRepository;

//     @Override
//     @Transactional
//     public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
//         log.info("Creating OTC order for customer: {}", orderRequest.getCustomerId());

//         Customer customer = customerRepository.findById(orderRequest.getCustomerId())
//                 .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

//         String orderCode = generateOrderCode();

//         Map<Long, List<OrderItemDTO>> itemsByPharmacy = orderRequest.getItems().stream()
//                 .collect(Collectors.groupingBy(OrderItemDTO::getPharmacyId));

//         CustomerOrder customerOrder = CustomerOrder.builder()
//                 .orderCode(orderCode)
//                 .customer(customer)
//                 .status(CustomerOrderStatus.PENDING)
//                 .paymentMethod(orderRequest.getPaymentMethod())
//                 .paymentStatus(PaymentStatus.PENDING)
//                 .subtotal(BigDecimal.ZERO)
//                 .discount(BigDecimal.ZERO)
//                 .tax(BigDecimal.ZERO)
//                 .shipping(BigDecimal.ZERO)
//                 .total(BigDecimal.ZERO)
//                 .currency("LKR")
//                 .pharmacyOrders(new ArrayList<>())
//                 .build();

//         BigDecimal grandTotal = BigDecimal.ZERO;

//         for (Map.Entry<Long, List<OrderItemDTO>> entry : itemsByPharmacy.entrySet()) {
//             Long pharmacyId = entry.getKey();
//             List<OrderItemDTO> pharmacyItems = entry.getValue();

//             Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
//                     .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found: " + pharmacyId));

//             PharmacyOrder pharmacyOrder = PharmacyOrder.builder()
//                     .customerOrder(customerOrder)
//                     .pharmacy(pharmacy)
//                     .status(PharmacyOrderStatus.RECEIVED)
//                     .subtotal(BigDecimal.ZERO)
//                     .discount(BigDecimal.ZERO)
//                     .tax(BigDecimal.ZERO)
//                     .shipping(BigDecimal.ZERO)
//                     .total(BigDecimal.ZERO)
//                     .items(new ArrayList<>())
//                     .build();

//             BigDecimal pharmacySubtotal = BigDecimal.ZERO;

//             for (OrderItemDTO itemDTO : pharmacyItems) {
//                 Otc otcProduct = otcRepository.findById(itemDTO.getOtcProductId())
//                         .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDTO.getOtcProductId()));

//                 if (otcProduct.getStock() < itemDTO.getQuantity()) {
//                     throw new IllegalStateException(
//                         String.format("Insufficient stock for %s. Available: %d, Requested: %d",
//                             otcProduct.getName(), otcProduct.getStock(), itemDTO.getQuantity())
//                     );
//                 }

//                 BigDecimal unitPrice = otcProduct.getPrice();
//                 BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

//                 PharmacyOrderItem orderItem = PharmacyOrderItem.builder()
//                         .pharmacyOrder(pharmacyOrder)
//                         .medicineName(otcProduct.getName())
//                         .dosage(otcProduct.getDosage())
//                         .quantity(itemDTO.getQuantity())
//                         .unitPrice(unitPrice)
//                         .totalPrice(totalPrice)
//                         .build();

//                 pharmacyOrder.getItems().add(orderItem);
//                 pharmacySubtotal = pharmacySubtotal.add(totalPrice);

//                 otcProduct.setStock(otcProduct.getStock() - itemDTO.getQuantity());
//                 otcRepository.save(otcProduct);
//             }

//             pharmacyOrder.setSubtotal(pharmacySubtotal);
//             pharmacyOrder.setTotal(pharmacySubtotal);
//             customerOrder.getPharmacyOrders().add(pharmacyOrder);
//             grandTotal = grandTotal.add(pharmacySubtotal);
//         }

//         customerOrder.setSubtotal(grandTotal);
//         customerOrder.setTotal(grandTotal);

//         CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
//         log.info("OTC Order created: {}, Total: Rs.{}", orderCode, grandTotal);

//         return mapToOrderResponse(savedOrder);
//     }

//     @Override
//     public OrderResponseDTO getOrderById(Long orderId) {
//         CustomerOrder order = customerOrderRepository.findById(orderId)
//                 .orElseThrow(() -> new EntityNotFoundException("Order not found"));
//         return mapToOrderResponse(order);
//     }

//     @Override
//     public OrderResponseDTO getOrderByCode(String orderCode) {
//         Optional<CustomerOrder> order = customerOrderRepository.findByOrderCode(orderCode);
//         return mapToOrderResponse(order.orElseThrow(() -> new EntityNotFoundException("Order not found")));
//     }

//     @Override
//     public List<OrderResponseDTO> getCustomerOrders(Long customerId) {
//         List<CustomerOrder> orders = customerOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
//         return orders.stream()
//                 .map(this::mapToOrderResponse)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     @Transactional
//     public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
//         CustomerOrder order = customerOrderRepository.findById(orderId)
//                 .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
//         order.setStatus(CustomerOrderStatus.valueOf(status));
//         CustomerOrder updatedOrder = customerOrderRepository.save(order);
        
//         return mapToOrderResponse(updatedOrder);
//     }

//     private String generateOrderCode() {
//         return "OTC-" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
//     }

//     private OrderResponseDTO mapToOrderResponse(CustomerOrder order) {
//         List<PharmacyOrderDTO> pharmacyOrderDTOs = order.getPharmacyOrders().stream()
//                 .map(this::mapToPharmacyOrderDTO)
//                 .collect(Collectors.toList());

//         return OrderResponseDTO.builder()
//                 .orderId(order.getId())
//                 .orderCode(order.getOrderCode())
//                 .status(order.getStatus().name())
//                 .total(order.getTotal())
//                 .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
//                 .paymentStatus(order.getPaymentStatus().name())
//                 .createdAt(order.getCreatedAt())
//                 .pharmacyOrders(pharmacyOrderDTOs)
//                 .build();
//     }

//     private PharmacyOrderDTO mapToPharmacyOrderDTO(PharmacyOrder pharmacyOrder) {
//         List<OrderItemDetailDTO> itemDTOs = pharmacyOrder.getItems().stream()
//                 .map(this::mapToOrderItemDetailDTO)
//                 .collect(Collectors.toList());

//         return PharmacyOrderDTO.builder()
//                 .pharmacyOrderId(pharmacyOrder.getId())
//                 .pharmacyId(pharmacyOrder.getPharmacy().getId())
//                 .pharmacyName(pharmacyOrder.getPharmacy().getName())
//                 .subtotal(pharmacyOrder.getSubtotal())
//                 .status(pharmacyOrder.getStatus().name())
//                 .items(itemDTOs)
//                 .build();
//     }

//     private OrderItemDetailDTO mapToOrderItemDetailDTO(PharmacyOrderItem item) {
//         return OrderItemDetailDTO.builder()
//                 .itemId(item.getId())
//                 .medicineName(item.getMedicineName())
//                 .dosage(item.getDosage())
//                 .quantity(item.getQuantity())
//                 .unitPrice(item.getUnitPrice())
//                 .totalPrice(item.getTotalPrice())
//                 .build();
//     }
// }






































// package com.leo.pillpathbackend.service.impl;

// import com.leo.pillpathbackend.dto.otcorder.*;
// import com.leo.pillpathbackend.entity.*;
// import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
// import com.leo.pillpathbackend.entity.enums.PaymentStatus;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
// import com.leo.pillpathbackend.repository.*;
// import com.leo.pillpathbackend.service.OtcOrderService;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class OtcOrderServiceImpl implements OtcOrderService {

//     private final CustomerOrderRepository customerOrderRepository;
//     private final PharmacyOrderRepository pharmacyOrderRepository;
//     private final CustomerRepository customerRepository;
//     private final PharmacyRepository pharmacyRepository;
//     private final OtcRepository otcRepository;

//     @Override
//     @Transactional
//     public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
//         log.info("Creating OTC order for customer: {}", orderRequest.getCustomerId());

//         // Validate customer
//         Customer customer = customerRepository.findById(orderRequest.getCustomerId())
//                 .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

//         // Generate order code
//         String orderCode = generateOrderCode();

//         // Group items by pharmacy
//         Map<Long, List<OrderItemDTO>> itemsByPharmacy = orderRequest.getItems().stream()
//                 .collect(Collectors.groupingBy(OrderItemDTO::getPharmacyId));

//         log.info("Order contains items from {} pharmacies", itemsByPharmacy.size());

//         // Create customer order (NO PRESCRIPTION - this is OTC)
//         CustomerOrder customerOrder = CustomerOrder.builder()
//                 .orderCode(orderCode)
//                 .customer(customer)
//                 .prescription(null)  // ✅ NULL for OTC orders
//                 .status(CustomerOrderStatus.PENDING)
//                 .paymentMethod(orderRequest.getPaymentMethod())
//                 .paymentStatus(PaymentStatus.PENDING)
//                 .deliveryAddress(orderRequest.getDeliveryAddress())
//                 .notes(orderRequest.getNotes())
//                 .subtotal(BigDecimal.ZERO)
//                 .discount(BigDecimal.ZERO)
//                 .tax(BigDecimal.ZERO)
//                 .shipping(BigDecimal.ZERO)
//                 .total(BigDecimal.ZERO)
//                 .currency("LKR")
//                 .pharmacyOrders(new ArrayList<>())
//                 .build();

//         BigDecimal grandTotal = BigDecimal.ZERO;

//         // Create pharmacy orders
//         for (Map.Entry<Long, List<OrderItemDTO>> entry : itemsByPharmacy.entrySet()) {
//             Long pharmacyId = entry.getKey();
//             List<OrderItemDTO> pharmacyItems = entry.getValue();

//             Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
//                     .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found: " + pharmacyId));

//             PharmacyOrder pharmacyOrder = PharmacyOrder.builder()
//                     .orderCode(generatePharmacyOrderCode(orderCode, pharmacyId))
//                     .customerOrder(customerOrder)
//                     .pharmacy(pharmacy)
//                     .submission(null)  // ✅ NULL for OTC orders (no prescription submission)
//                     .status(PharmacyOrderStatus.RECEIVED)
//                     .pickupCode(generatePickupCode(pharmacyId))
//                     .pickupLocation(pharmacy.getAddress())
//                     .customerNote(orderRequest.getNotes())
//                     .subtotal(BigDecimal.ZERO)
//                     .discount(BigDecimal.ZERO)
//                     .tax(BigDecimal.ZERO)
//                     .shipping(BigDecimal.ZERO)
//                     .total(BigDecimal.ZERO)
//                     .items(new ArrayList<>())
//                     .build();

//             BigDecimal pharmacySubtotal = BigDecimal.ZERO;

//             // Create order items
//             for (OrderItemDTO itemDTO : pharmacyItems) {
//                 Otc otcProduct = otcRepository.findById(itemDTO.getOtcProductId())
//                         .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDTO.getOtcProductId()));

//                 // Validate stock
//                 if (otcProduct.getStock() < itemDTO.getQuantity()) {
//                     throw new IllegalStateException(
//                         String.format("Insufficient stock for %s. Available: %d, Requested: %d",
//                             otcProduct.getName(), otcProduct.getStock(), itemDTO.getQuantity())
//                     );
//                 }

//                 BigDecimal unitPrice = otcProduct.getPrice();
//                 BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

//                 PharmacyOrderItem orderItem = PharmacyOrderItem.builder()
//                         .pharmacyOrder(pharmacyOrder)
//                         .submissionItem(null)  // ✅ NULL for OTC orders
//                         .medicineName(otcProduct.getName())
//                         .genericName(otcProduct.getName())
//                         .dosage(otcProduct.getDosage())
//                         .quantity(itemDTO.getQuantity())
//                         .unitPrice(unitPrice)
//                         .totalPrice(totalPrice)
//                         .build();

//                 pharmacyOrder.getItems().add(orderItem);
//                 pharmacySubtotal = pharmacySubtotal.add(totalPrice);

//                 // Update stock
//                 otcProduct.setStock(otcProduct.getStock() - itemDTO.getQuantity());
//                 otcRepository.save(otcProduct);

//                 log.info("Added: {} x{} = Rs.{}", otcProduct.getName(), itemDTO.getQuantity(), totalPrice);
//             }

//             pharmacyOrder.setSubtotal(pharmacySubtotal);
//             pharmacyOrder.setTotal(pharmacySubtotal);
//             customerOrder.getPharmacyOrders().add(pharmacyOrder);
//             grandTotal = grandTotal.add(pharmacySubtotal);
//         }

//         customerOrder.setSubtotal(grandTotal);
//         customerOrder.setTotal(grandTotal);

//         CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
//         log.info("OTC Order created: {}, Total: Rs.{}", orderCode, grandTotal);

//         return mapToOrderResponse(savedOrder);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public OrderResponseDTO getOrderById(Long orderId) {
//         CustomerOrder order = customerOrderRepository.findById(orderId)
//                 .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
//         // Only return OTC orders (no prescription)
//         if (order.getPrescription() != null) {
//             throw new EntityNotFoundException("This is not an OTC order");
//         }
        
//         return mapToOrderResponse(order);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public OrderResponseDTO getOrderByCode(String orderCode) {
//         CustomerOrder order = customerOrderRepository.findByOrderCode(orderCode)
//                 .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
//         // Only return OTC orders (no prescription)
//         if (order.getPrescription() != null) {
//             throw new EntityNotFoundException("This is not an OTC order");
//         }
        
//         return mapToOrderResponse(order);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<OrderResponseDTO> getCustomerOrders(Long customerId) {
//         List<CustomerOrder> orders = customerOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        
//         // Filter only OTC orders (no prescription)
//         return orders.stream()
//                 .filter(order -> order.getPrescription() == null)
//                 .map(this::mapToOrderResponse)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     @Transactional
//     public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
//         CustomerOrder order = customerOrderRepository.findById(orderId)
//                 .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
//         // Only update OTC orders
//         if (order.getPrescription() != null) {
//             throw new IllegalStateException("Cannot update prescription order through OTC endpoint");
//         }
        
//         order.setStatus(CustomerOrderStatus.valueOf(status));
//         CustomerOrder updatedOrder = customerOrderRepository.save(order);
        
//         log.info("OTC Order {} status updated to {}", orderId, status);
//         return mapToOrderResponse(updatedOrder);
//     }

//     // ==================== HELPER METHODS ====================

//     private String generateOrderCode() {
//         return "OTC-" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
//     }

//     private String generatePharmacyOrderCode(String orderCode, Long pharmacyId) {
//         return orderCode + "-P" + pharmacyId;
//     }

//     private String generatePickupCode(Long pharmacyId) {
//         return "PU-OTC-" + pharmacyId + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
//     }

//     private OrderResponseDTO mapToOrderResponse(CustomerOrder order) {
//         List<PharmacyOrderDTO> pharmacyOrderDTOs = order.getPharmacyOrders().stream()
//                 .map(this::mapToPharmacyOrderDTO)
//                 .collect(Collectors.toList());

//         return OrderResponseDTO.builder()
//                 .orderId(order.getId())
//                 .orderCode(order.getOrderCode())
//                 .status(order.getStatus().name())
//                 .total(order.getTotal())
//                 .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
//                 .paymentStatus(order.getPaymentStatus().name())
//                 .deliveryAddress(order.getDeliveryAddress())
//                 .createdAt(order.getCreatedAt())
//                 .pharmacyOrders(pharmacyOrderDTOs)
//                 .build();
//     }

//     private PharmacyOrderDTO mapToPharmacyOrderDTO(PharmacyOrder pharmacyOrder) {
//         List<OrderItemDetailDTO> itemDTOs = pharmacyOrder.getItems().stream()
//                 .map(this::mapToOrderItemDetailDTO)
//                 .collect(Collectors.toList());

//         return PharmacyOrderDTO.builder()
//                 .pharmacyOrderId(pharmacyOrder.getId())
//                 .orderCode(pharmacyOrder.getOrderCode())
//                 .pharmacyId(pharmacyOrder.getPharmacy().getId())
//                 .pharmacyName(pharmacyOrder.getPharmacy().getName())
//                 .pharmacyAddress(pharmacyOrder.getPharmacy().getAddress())
//                 .pharmacyPhone(pharmacyOrder.getPharmacy().getPhoneNumber())
//                 .subtotal(pharmacyOrder.getSubtotal())
//                 .status(pharmacyOrder.getStatus().name())
//                 .items(itemDTOs)
//                 .build();
//     }

//     private OrderItemDetailDTO mapToOrderItemDetailDTO(PharmacyOrderItem item) {
//         return OrderItemDetailDTO.builder()
//                 .itemId(item.getId())
//                 .medicineName(item.getMedicineName())
//                 .genericName(item.getGenericName())
//                 .dosage(item.getDosage())
//                 .quantity(item.getQuantity())
//                 .unitPrice(item.getUnitPrice())
//                 .totalPrice(item.getTotalPrice())
//                 .build();
//     }
// }