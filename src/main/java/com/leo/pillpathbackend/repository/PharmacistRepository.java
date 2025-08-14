package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Pharmacist;
import com.leo.pillpathbackend.entity.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {
    Optional<Pharmacist> findByEmail(String email);
    Optional<Pharmacist> findByLicenseNumber(String licenseNumber);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
    
    // Find pharmacists by pharmacy
    List<Pharmacist> findByPharmacyId(Long pharmacyId);
    List<Pharmacist> findByPharmacyIdAndIsActiveTrue(Long pharmacyId);
    List<Pharmacist> findByPharmacyIdAndEmploymentStatus(Long pharmacyId, EmploymentStatus employmentStatus);
    
    // Search functionality
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND " +
           "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Pharmacist> findByPharmacyIdAndSearchTerm(@Param("pharmacyId") Long pharmacyId, 
                                                  @Param("searchTerm") String searchTerm);
    
    // Count pharmacists by pharmacy
    long countByPharmacyId(Long pharmacyId);
    long countByPharmacyIdAndIsActiveTrue(Long pharmacyId);
}