package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.CustomerOrder;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByOrderCodeAndCustomerId(String orderCode, Long customerId);
    boolean existsByPrescriptionIdAndCustomerIdAndStatusIn(Long prescriptionId, Long customerId, Collection<CustomerOrderStatus> statuses);

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

        // ✅ ADD THESE TWO NEW METHODS
    Optional<CustomerOrder> findByOrderCode(String orderCode);
    // List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByCustomerId(Long customerId);

    long countByStatus(CustomerOrderStatus status);
    long countByStatusIn(java.util.Collection<com.leo.pillpathbackend.entity.enums.CustomerOrderStatus> statuses);
    long countByStatusInAndCreatedAtBetween(java.util.Collection<CustomerOrderStatus> statuses, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM CustomerOrder o")
    double sumTotal();
}
