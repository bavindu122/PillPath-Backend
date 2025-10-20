package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyOrder;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
    
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
}






















// package com.leo.pillpathbackend.repository;

// import com.leo.pillpathbackend.entity.PharmacyOrder;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
// import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.math.BigDecimal;
// import java.util.Collection;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
    
//     List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    
//     List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
    
//     Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
//     List<PharmacyOrder> findBySubmissionIdIn(List<Long> submissionIds);
    
//     boolean existsBySubmissionIdAndCustomerOrder_StatusIn(Long submissionId, Collection<CustomerOrderStatus> statuses);

//     List<PharmacyOrder> findByCustomerOrderId(Long customerOrderId);

//     // ✅ Dashboard Statistics Queries - CORRECTED
    
//     // Count total orders by pharmacyId field (not relationship)
//     Long countByPharmacyId(Long pharmacyId);

//     // Calculate total revenue for completed orders
//     @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PharmacyOrder po " +
//            "WHERE po.pharmacyId = :pharmacyId " +
//            "AND po.status = 'COMPLETED'")
//     BigDecimal calculateTotalRevenueByPharmacyId(@Param("pharmacyId") Long pharmacyId);

//     // Count orders by pharmacy and status
//     Long countByPharmacyIdAndStatus(Long pharmacyId, PharmacyOrderStatus status);
// }





































// package com.leo.pillpathbackend.repository;

// import com.leo.pillpathbackend.entity.PharmacyOrder;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
// import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.math.BigDecimal;
// import java.util.Collection;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
    
//     List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    
//     List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
    
//     Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
//     List<PharmacyOrder> findBySubmissionIdIn(List<Long> submissionIds);
    
//     boolean existsBySubmissionIdAndCustomerOrder_StatusIn(Long submissionId, Collection<CustomerOrderStatus> statuses);

//     List<PharmacyOrder> findByCustomerOrderId(Long customerOrderId);

//     // Dashboard Statistics Queries
    
//     @Query("SELECT COUNT(po) FROM PharmacyOrder po WHERE po.pharmacy.id = :pharmacyId")
//     Long countTotalOrdersByPharmacyId(@Param("pharmacyId") Long pharmacyId);

//     @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PharmacyOrder po " +
//            "WHERE po.pharmacy.id = :pharmacyId " +
//            "AND po.status = 'COMPLETED'")
//     BigDecimal calculateTotalRevenueByPharmacyId(@Param("pharmacyId") Long pharmacyId);

//     @Query("SELECT COUNT(po) FROM PharmacyOrder po " +
//            "WHERE po.pharmacy.id = :pharmacyId " +
//            "AND po.status = :status")
//     Long countOrdersByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId, 
//                                           @Param("status") PharmacyOrderStatus status);
// }



































// package com.leo.pillpathbackend.repository;

// import com.leo.pillpathbackend.entity.PharmacyOrder;
// import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
// import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus; // added
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.Collection; // added
// import java.util.List;
// import java.util.Optional;
// import java.math.BigDecimal;

// public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
//     List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
//     List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
//     Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
//     List<PharmacyOrder> findBySubmissionIdIn(List<Long> submissionIds);
//     // Check if there exists a pharmacy order for a submission whose parent customer order is in active statuses
//     boolean existsBySubmissionIdAndCustomerOrder_StatusIn(Long submissionId, Collection<CustomerOrderStatus> statuses);

//     // New: fetch all slices for a parent customer order id
//     List<PharmacyOrder> findByCustomerOrderId(Long customerOrderId);

//         // Dashboard Statistics Queries
    
//     @Query("SELECT COUNT(po) FROM PharmacyOrder po WHERE po.pharmacy.id = :pharmacyId")
//     Long countTotalOrdersByPharmacyId(@Param("pharmacyId") Long pharmacyId);

//     @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PharmacyOrder po " +
//            "WHERE po.pharmacy.id = :pharmacyId " +
//            "AND po.status = 'COMPLETED'")
//     BigDecimal calculateTotalRevenueByPharmacyId(@Param("pharmacyId") Long pharmacyId);

//     @Query("SELECT COUNT(po) FROM PharmacyOrder po " +
//            "WHERE po.pharmacy.id = :pharmacyId " +
//            "AND po.status = :status")
//     Long countOrdersByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId, 
//                                           @Param("status") PharmacyOrderStatus status);
// }