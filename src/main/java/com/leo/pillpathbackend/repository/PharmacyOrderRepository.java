package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyOrder;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus; // added
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection; // added
import java.util.List;
import java.util.Optional;

public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long>, JpaSpecificationExecutor<PharmacyOrder> {
    List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
    Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
    List<PharmacyOrder> findBySubmissionIdIn(List<Long> submissionIds);
    // Check if there exists a pharmacy order for a submission whose parent customer order is in active statuses
    boolean existsBySubmissionIdAndCustomerOrder_StatusIn(Long submissionId, Collection<CustomerOrderStatus> statuses);

    // New: fetch all slices for a parent customer order id
    List<PharmacyOrder> findByCustomerOrderId(Long customerOrderId);

    // New: resolve pharmacy order by public code
    Optional<PharmacyOrder> findByOrderCode(String orderCode);

    // New: resolve by code scoped to a customer
    Optional<PharmacyOrder> findByOrderCodeAndCustomerOrder_Customer_Id(String orderCode, Long customerId);

    // Count fulfilled (delivered) orders all-time for a pharmacy
    long countByPharmacyIdAndStatus(Long pharmacyId, PharmacyOrderStatus status);
}