package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PayoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PayoutRecordRepository extends JpaRepository<PayoutRecord, Long>, JpaSpecificationExecutor<PayoutRecord> {
    Optional<PayoutRecord> findByOrderId(Long orderId);
}
