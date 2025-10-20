package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.entity.CustomerOrder;

import java.math.BigDecimal;

public interface LoyaltyService {
    /**
     * Calculate and award loyalty points for a completed order paid by card
     */
    Integer calculateAndAwardPoints(CustomerOrder order);
    
    /**
     * Get current loyalty points rate (points per LKR)
     */
    BigDecimal getLoyaltyPointsRate();
    
    /**
     * Update loyalty points rate (admin only)
     */
    void updateLoyaltyPointsRate(BigDecimal rate);
    
    /**
     * Get customer's current loyalty points
     */
    Integer getCustomerLoyaltyPoints(Long customerId);
    
    /**
     * Add points to customer account
     */
    void addPointsToCustomer(Long customerId, Integer points);
    
    /**
     * Deduct points from customer account
     */
    void deductPointsFromCustomer(Long customerId, Integer points);
}
