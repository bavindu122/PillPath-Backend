package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Pharmacist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {
    
    Optional<Pharmacist> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByLicenseNumber(String licenseNumber);
    
    List<Pharmacist> findByPharmacyId(Long pharmacyId);
    
    List<Pharmacist> findByPharmacyIdAndIsActiveTrue(Long pharmacyId);
    
    long countByPharmacyIdAndIsActiveTrue(Long pharmacyId);
    
    boolean existsByIdAndPharmacyIdAndIsActiveTrue(Long pharmacistId, Long pharmacyId);
    
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true AND p.licenseExpiryDate < :date")
    List<Pharmacist> findByPharmacyIdAndLicenseExpiringSoon(@Param("pharmacyId") Long pharmacyId, @Param("date") LocalDate date);
    
    List<Pharmacist> findBySpecializationContainingIgnoreCase(String specialization);
    
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.shiftSchedule = :shiftSchedule AND p.isActive = true")
    List<Pharmacist> findByPharmacyIdAndShiftSchedule(@Param("pharmacyId") Long pharmacyId, @Param("shiftSchedule") String shiftSchedule);
    
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true AND " +
           "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.licenseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.specialization) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Pharmacist> searchByPharmacyIdAndTerm(@Param("pharmacyId") Long pharmacyId, @Param("searchTerm") String searchTerm);
}