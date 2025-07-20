package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByEmail(String email);
    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
}