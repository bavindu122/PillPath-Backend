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

    // Find all active pharmacists for a pharmacy
    List<Pharmacist> findByPharmacyIdAndIsActiveTrue(Long pharmacyId);

    // Find all pharmacists for a pharmacy
    List<Pharmacist> findByPharmacyId(Long pharmacyId);

    // Check if email exists (direct field)
    boolean existsByEmail(String email);

    // Check if a license number exists
    boolean existsByLicenseNumber(String licenseNumber);

    // Find pharmacist by email (direct field)
    Optional<Pharmacist> findByEmail(String email);

    // Find pharmacist by license number
    Optional<Pharmacist> findByLicenseNumber(String licenseNumber);

    // Find pharmacist by user email (through PharmacistUser relationship)
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacistUser.email = :email")
    Optional<Pharmacist> findByUserEmail(@Param("email") String email);

    // Find all active pharmacists for a pharmacy (custom query)
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true")
    List<Pharmacist> findActivePharmacistsByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    // Count active pharmacists for a pharmacy
    @Query("SELECT COUNT(p) FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true")
    Long countActivePharmacistsByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    // Count by pharmacy and active status
    long countByPharmacyIdAndIsActiveTrue(Long pharmacyId);

    // Check if pharmacist belongs to pharmacy and is active
    boolean existsByIdAndPharmacyIdAndIsActiveTrue(Long pharmacistId, Long pharmacyId);

    // Search by pharmacy and term (using direct fields)
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true AND " +
           "(LOWER(p.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.licenseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.specialization) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Pharmacist> searchByPharmacyIdAndTerm(@Param("pharmacyId") Long pharmacyId, @Param("searchTerm") String searchTerm);

    // Find by specialization (case-insensitive)
    List<Pharmacist> findBySpecializationContainingIgnoreCase(String specialization);

    // Find by pharmacy and shift schedule
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.shiftSchedule = :shiftSchedule AND p.isActive = true")
    List<Pharmacist> findByPharmacyIdAndShiftSchedule(@Param("pharmacyId") Long pharmacyId, @Param("shiftSchedule") String shiftSchedule);

    // Find pharmacists with license expiring soon
    @Query("SELECT p FROM Pharmacist p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true AND p.licenseExpiryDate < :date")
    List<Pharmacist> findByPharmacyIdAndLicenseExpiringSoon(@Param("pharmacyId") Long pharmacyId, @Param("date") LocalDate date);
}