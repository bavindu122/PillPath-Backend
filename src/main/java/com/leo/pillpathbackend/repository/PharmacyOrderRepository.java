package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyOrder;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long>, JpaSpecificationExecutor<PharmacyOrder> {
    List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    
    List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
    
    Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
    List<PharmacyOrder> findBySubmissionIdIn(List<Long> submissionIds);
    
    boolean existsBySubmissionIdAndCustomerOrder_StatusIn(Long submissionId, Collection<CustomerOrderStatus> statuses);

    List<PharmacyOrder> findByCustomerOrderId(Long customerOrderId);

    // Dashboard Statistics Queries
    
    // Count total orders by pharmacyId
    Long countByPharmacyId(Long pharmacyId);

    // ✅ FIXED: Use 'total' instead of 'totalAmount' and 'pharmacy.id' instead of 'pharmacyId'
    @Query("SELECT COALESCE(SUM(po.total), 0) FROM PharmacyOrder po " +
           "WHERE po.pharmacy.id = :pharmacyId " +
           "AND po.status = 'HANDED_OVER'")
    BigDecimal calculateTotalRevenueByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    // Count orders by pharmacy and status
    @Query("SELECT COUNT(po) FROM PharmacyOrder po WHERE po.pharmacy.id = :pharmacyId AND po.status = :status")
    Long countByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId, @Param("status") PharmacyOrderStatus status);
    // New: resolve pharmacy order by public code
    Optional<PharmacyOrder> findByOrderCode(String orderCode);

    // New: resolve by code scoped to a customer
    Optional<PharmacyOrder> findByOrderCodeAndCustomerOrder_Customer_Id(String orderCode, Long customerId);
}



