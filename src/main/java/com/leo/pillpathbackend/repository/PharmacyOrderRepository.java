package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyOrder;
import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {
    List<PharmacyOrder> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    List<PharmacyOrder> findByPharmacyIdAndStatusOrderByCreatedAtDesc(Long pharmacyId, PharmacyOrderStatus status);
    Optional<PharmacyOrder> findByIdAndPharmacyId(Long id, Long pharmacyId);
}

