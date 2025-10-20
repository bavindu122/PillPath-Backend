package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Prescription;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Prescription> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);

    @Query("SELECT p FROM Prescription p WHERE p.pharmacy.id = :pharmacyId AND p.status IN :statuses ORDER BY p.createdAt DESC")
    List<Prescription> findByPharmacyIdAndStatuses(@Param("pharmacyId") Long pharmacyId,
                                                   @Param("statuses") Collection<PrescriptionStatus> statuses);

    Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId);
    Optional<Prescription> findByIdAndPharmacyId(Long id, Long pharmacyId);
    Optional<Prescription> findByCode(String code);

    Page<Prescription> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    long countByCustomerId(Long customerId);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    List<Prescription> findByCustomerIdAndFamilyMemberIdOrderByCreatedAtDesc(Long customerId, Long familyMemberId);
}
