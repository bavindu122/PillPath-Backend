package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyOrder;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus; // added
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection; // added
import java.util.List;
import java.util.Optional;

public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
    List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
    Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
    List<PharmacyOrder> findBySubmissionIdIn(List<Long> submissionIds);
    // Check if there exists a pharmacy order for a submission whose parent customer order is in active statuses
    boolean existsBySubmissionIdAndCustomerOrder_StatusIn(Long submissionId, Collection<CustomerOrderStatus> statuses);

    // New: fetch all slices for a parent customer order id
    List<PharmacyOrder> findByCustomerOrderId(Long customerOrderId);
    
    // Count orders created on a specific date (between start and end of day)
    @Query("SELECT COUNT(po) FROM PharmacyOrder po WHERE po.createdAt >= :startOfDay AND po.createdAt < :endOfDay")
    long countByCreatedAtBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}