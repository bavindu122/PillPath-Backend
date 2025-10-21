package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    // Find all transactions for a customer, ordered by date
    List<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Find transaction by order ID
    Optional<LoyaltyTransaction> findByOrderId(Long orderId);

    // Check if transaction already exists for an order
    boolean existsByOrderId(Long orderId);

    // Calculate total points for a customer
    @Query("SELECT COALESCE(SUM(lt.pointsEarned), 0) FROM LoyaltyTransaction lt WHERE lt.customer.id = :customerId")
    Integer calculateTotalPointsByCustomerId(@Param("customerId") Long customerId);
    
    // Calculate total points for a customer excluding cancelled orders
    @Query("SELECT COALESCE(SUM(lt.pointsEarned), 0) FROM LoyaltyTransaction lt " +
           "WHERE lt.customer.id = :customerId AND lt.order.status != 'CANCELLED'")
    Integer calculateTotalPointsByCustomerIdExcludingCancelled(@Param("customerId") Long customerId);
}
