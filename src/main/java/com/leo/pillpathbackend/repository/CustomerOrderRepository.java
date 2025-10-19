package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.CustomerOrder;
import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
