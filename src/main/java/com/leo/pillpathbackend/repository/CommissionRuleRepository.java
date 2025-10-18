package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {
    Optional<CommissionRule> findByPharmacy_Id(Long pharmacyId);
    void deleteByPharmacy_Id(Long pharmacyId);
}

