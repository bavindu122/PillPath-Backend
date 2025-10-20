package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.entity.Customer;
import com.leo.pillpathbackend.entity.CustomerOrder;
import com.leo.pillpathbackend.entity.LoyaltyTransaction;
import com.leo.pillpathbackend.entity.SystemSettings;
import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import com.leo.pillpathbackend.entity.enums.PaymentStatus;
import com.leo.pillpathbackend.repository.CustomerRepository;
import com.leo.pillpathbackend.repository.LoyaltyTransactionRepository;
import com.leo.pillpathbackend.repository.SystemSettingsRepository;
import com.leo.pillpathbackend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyServiceImpl implements LoyaltyService {

    private final CustomerRepository customerRepository;
    private final SystemSettingsRepository systemSettingsRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Override
    @Transactional
    public Integer calculateAndAwardPoints(CustomerOrder order) {
        // Only award points for card payments (credit/debit) that are paid
        boolean isCardPayment = order.getPaymentMethod() == PaymentMethod.CREDIT_CARD || 
                                order.getPaymentMethod() == PaymentMethod.DEBIT_CARD;
        
        if (!isCardPayment || order.getPaymentStatus() != PaymentStatus.PAID) {
            log.info("No loyalty points awarded - Payment method: {}, Status: {}", 
                    order.getPaymentMethod(), order.getPaymentStatus());
            return 0;
        }

        // Check if transaction already exists for this order
        if (loyaltyTransactionRepository.existsByOrderId(order.getId())) {
            log.info("Loyalty points already awarded for order {}", order.getOrderCode());
            return loyaltyTransactionRepository.findByOrderId(order.getId())
                    .map(LoyaltyTransaction::getPointsEarned)
                    .orElse(0);
        }

        // Get current loyalty rate at the time of transaction
        BigDecimal rate = getLoyaltyPointsRate();
        BigDecimal orderTotal = order.getTotal();
        
        // Points = orderTotal * rate, rounded down
        Integer points = orderTotal.multiply(rate)
                .setScale(0, RoundingMode.DOWN)
                .intValue();

        if (points > 0) {
            // Save loyalty transaction with the rate at this moment
            LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                    .customer(order.getCustomer())
                    .order(order)
                    .orderTotal(orderTotal)
                    .loyaltyRate(rate)
                    .pointsEarned(points)
                    .paymentMethod(order.getPaymentMethod().toString())
                    .build();
            
            loyaltyTransactionRepository.save(transaction);
            
            // Update customer's total points
            addPointsToCustomer(order.getCustomer().getId(), points);
            
            log.info("Awarded {} loyalty points to customer {} for order {} at rate {}", 
                    points, order.getCustomer().getId(), order.getOrderCode(), rate);
        }

        return points;
    }

    @Override
    public BigDecimal getLoyaltyPointsRate() {
        return systemSettingsRepository
                .findBySettingKey(SystemSettings.LOYALTY_POINTS_RATE_KEY)
                .map(setting -> new BigDecimal(setting.getSettingValue()))
                .orElse(new BigDecimal(SystemSettings.DEFAULT_LOYALTY_RATE));
    }

    @Override
    @Transactional
    public void updateLoyaltyPointsRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Loyalty points rate cannot be negative");
        }

        SystemSettings setting = systemSettingsRepository
                .findBySettingKey(SystemSettings.LOYALTY_POINTS_RATE_KEY)
                .orElse(SystemSettings.builder()
                        .settingKey(SystemSettings.LOYALTY_POINTS_RATE_KEY)
                        .description("Loyalty points earned per 1 LKR spent (card payments only)")
                        .build());

        setting.setSettingValue(rate.toString());
        systemSettingsRepository.save(setting);
        
        log.info("Updated loyalty points rate to: {}", rate);
    }

    @Override
    public Integer getCustomerLoyaltyPoints(Long customerId) {
        // Calculate total points from all transactions excluding cancelled orders
        Integer totalPoints = loyaltyTransactionRepository.calculateTotalPointsByCustomerIdExcludingCancelled(customerId);
        return totalPoints != null ? totalPoints : 0;
    }

    @Override
    @Transactional
    public void addPointsToCustomer(Long customerId, Integer points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points to add must be positive");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Integer currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        customer.setLoyaltyPoints(currentPoints + points);
        customerRepository.save(customer);
        
        log.info("Added {} points to customer {}. New balance: {}", 
                points, customerId, customer.getLoyaltyPoints());
    }

    @Override
    @Transactional
    public void deductPointsFromCustomer(Long customerId, Integer points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points to deduct must be positive");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Integer currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        
        if (currentPoints < points) {
            throw new IllegalArgumentException("Insufficient loyalty points");
        }

        customer.setLoyaltyPoints(currentPoints - points);
        customerRepository.save(customer);
        
        log.info("Deducted {} points from customer {}. New balance: {}", 
                points, customerId, customer.getLoyaltyPoints());
    }
}
