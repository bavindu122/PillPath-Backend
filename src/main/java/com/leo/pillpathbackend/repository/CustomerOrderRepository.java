package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByOrderCodeAndCustomerId(String orderCode, Long customerId);
}

