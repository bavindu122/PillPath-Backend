package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PharmacyAdminRepository extends JpaRepository<PharmacyAdmin, Long> {
    Optional<PharmacyAdmin> findByEmail(String email);
    Optional<PharmacyAdmin> findByUsername(String username);
    List<PharmacyAdmin> findByPharmacyId(Long pharmacyId);
    Optional<PharmacyAdmin> findByPharmacyIdAndIsPrimaryAdmin(Long pharmacyId, Boolean isPrimaryAdmin);
}