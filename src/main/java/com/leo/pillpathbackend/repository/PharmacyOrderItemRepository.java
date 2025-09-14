package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyOrderItemRepository extends JpaRepository<PharmacyOrderItem, Long> {
}

