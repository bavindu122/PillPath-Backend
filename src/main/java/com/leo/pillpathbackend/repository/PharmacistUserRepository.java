package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacistUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacistUserRepository extends JpaRepository<PharmacistUser, Long> {

    List<PharmacistUser> findByPharmacyIdAndIsActiveTrue(Long pharmacyId);
    List<PharmacistUser> findByPharmacyId(Long pharmacyId);

    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<PharmacistUser> findByEmail(String email);
    Optional<PharmacistUser> findByLicenseNumber(String licenseNumber);

    boolean existsByIdAndPharmacyIdAndIsActiveTrue(Long id, Long pharmacyId);
    long countByPharmacyIdAndIsActiveTrue(Long pharmacyId);

    @Query("SELECT p FROM PharmacistUser p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true AND (" +
            "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.licenseNumber) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.specialization) LIKE LOWER(CONCAT('%', :term, '%')))" )
    List<PharmacistUser> search(@Param("pharmacyId") Long pharmacyId, @Param("term") String term);

    @Query("SELECT p FROM PharmacistUser p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true AND p.licenseExpiryDate < :date")
    List<PharmacistUser> findByPharmacyIdAndLicenseExpiringSoon(@Param("pharmacyId") Long pharmacyId, @Param("date") LocalDate date);

    // ✅ FIXED: Use 'isActive' instead of 'active'
    @Query("SELECT COUNT(p) FROM PharmacistUser p WHERE p.pharmacy.id = :pharmacyId AND p.isActive = true")
    Long countActivePharmacistsByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}

























