package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.LoyaltyPointsDTO;
import com.leo.pillpathbackend.dto.LoyaltyTransactionDTO;
import com.leo.pillpathbackend.entity.Customer;
import com.leo.pillpathbackend.entity.LoyaltyTransaction;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import com.leo.pillpathbackend.repository.CustomerRepository;
import com.leo.pillpathbackend.repository.LoyaltyTransactionRepository;
import com.leo.pillpathbackend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customer/loyalty")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerLoyaltyController {

    private final LoyaltyService loyaltyService;
    private final CustomerRepository customerRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @GetMapping
    public ResponseEntity<LoyaltyPointsDTO> getLoyaltyPoints(Authentication authentication) {
        // JWT filter sets principal as userId (string)
        Long userId = Long.parseLong(authentication.getName());
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        LoyaltyPointsDTO dto = LoyaltyPointsDTO.builder()
                .currentPoints(loyaltyService.getCustomerLoyaltyPoints(customer.getId()))
                .pointsRate(loyaltyService.getLoyaltyPointsRate())
                .rateDescription("Points earned per 1 LKR spent (card payments only)")
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/points")
    public ResponseEntity<Integer> getCurrentPoints(Authentication authentication) {
        // JWT filter sets principal as userId (string)
        Long userId = Long.parseLong(authentication.getName());
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Integer points = loyaltyService.getCustomerLoyaltyPoints(customer.getId());
        return ResponseEntity.ok(points);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<LoyaltyTransactionDTO>> getLoyaltyTransactions(Authentication authentication) {
        // JWT filter sets principal as userId (string)
        Long userId = Long.parseLong(authentication.getName());
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Fetch all loyalty transactions for this customer
        List<LoyaltyTransaction> transactions = loyaltyTransactionRepository
                .findByCustomerIdOrderByCreatedAtDesc(customer.getId());

        // Convert to DTOs - use the stored points earned at the time of transaction
        // Filter out cancelled orders
        List<LoyaltyTransactionDTO> transactionDTOs = transactions.stream()
                .filter(transaction -> transaction.getOrder().getStatus() != CustomerOrderStatus.CANCELLED)
                .map(transaction -> LoyaltyTransactionDTO.builder()
                        .orderCode(transaction.getOrder().getOrderCode())
                        .orderDate(transaction.getCreatedAt())
                        .orderTotal(transaction.getOrderTotal())
                        .paymentMethod(transaction.getPaymentMethod())
                        .pointsEarned(transaction.getPointsEarned()) // Points calculated at transaction time
                        .orderStatus(transaction.getOrder().getStatus() != null ? 
                                transaction.getOrder().getStatus().toString() : "PENDING")
                        .loyaltyRate(transaction.getLoyaltyRate()) // Include the rate used
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDTOs);
    }
}
