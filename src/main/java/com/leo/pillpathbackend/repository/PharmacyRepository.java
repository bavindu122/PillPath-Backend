package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    Optional<Pharmacy> findByEmail(String email);
    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);

    // Count methods for stats
    Long countByIsActiveTrueAndIsVerifiedTrue();
    Long countByIsActiveTrueAndIsVerifiedFalse();
    Long countByIsActiveFalseAndIsVerifiedTrue();
    Long countByIsActiveFalseAndIsVerifiedFalse();

    // Admin management queries
    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE p.isActive = true AND p.isVerified = true")
    Long countActivePharmacies();

    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE p.isActive = false AND p.isVerified = false")
    Long countRejectedPharmacies();

    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE p.isVerified = false AND p.isActive = true")
    Long countPendingApproval();

    @Query("SELECT COUNT(p) FROM Pharmacy p WHERE p.isActive = false AND p.isVerified = true")
    Long countSuspendedPharmacies();

    // Search and filter queries
    // Add these methods to PharmacyRepository.java


    @Query("SELECT p FROM Pharmacy p WHERE " +
            "(:searchTerm = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "(:status IS NULL OR " +
            "(:status = 'Active' AND p.isActive = true AND p.isVerified = true) OR " +
            "(:status = 'Pending' AND p.isActive = true AND p.isVerified = false) OR " +
            "(:status = 'Suspended' AND p.isActive = false AND p.isVerified = true) OR " +
            "(:status = 'Rejected' AND p.isActive = false AND p.isVerified = false))")
    Page<Pharmacy> findPharmaciesWithFilters(@Param("searchTerm") String searchTerm,
                                             @Param("status") String status,
                                             Pageable pageable);
    @Query("SELECT p FROM Pharmacy p ORDER BY p.createdAt DESC")
    List<Pharmacy> findTop10ByOrderByCreatedAtDesc();
}