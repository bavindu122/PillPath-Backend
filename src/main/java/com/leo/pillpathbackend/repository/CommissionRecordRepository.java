package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.CommissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, Long>, JpaSpecificationExecutor<CommissionRecord> {
    Optional<CommissionRecord> findByOrderId(Long orderId);
}
