package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PrescriptionSubmission;
import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionSubmissionRepository extends JpaRepository<PrescriptionSubmission, Long> {
    List<PrescriptionSubmission> findByPrescriptionId(Long prescriptionId);

    List<PrescriptionSubmission> findByPrescriptionIdIn(List<Long> prescriptionIds);

    @EntityGraph(attributePaths = {"pharmacy"})
    List<PrescriptionSubmission> findWithPharmacyByPrescriptionIdIn(List<Long> prescriptionIds);

    // Queue queries
    @EntityGraph(attributePaths = {"prescription"})
    List<PrescriptionSubmission> findByPharmacyIdAndStatusOrderByCreatedAtAsc(Long pharmacyId, PrescriptionStatus status);

    @EntityGraph(attributePaths = {"prescription"})
    List<PrescriptionSubmission> findByPharmacyIdAndStatusAndAssignedPharmacistIsNullOrderByCreatedAtAsc(Long pharmacyId, PrescriptionStatus status);

    @EntityGraph(attributePaths = {"prescription"})
    List<PrescriptionSubmission> findByPharmacyIdAndAssignedPharmacistIdAndStatusOrderByCreatedAtAsc(Long pharmacyId, Long pharmacistId, PrescriptionStatus status);

    // Atomic claim; returns number of rows updated
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PrescriptionSubmission s set s.assignedPharmacist.id = :pharmacistId, s.status = com.leo.pillpathbackend.entity.enums.PrescriptionStatus.IN_PROGRESS where s.id = :id and s.status = com.leo.pillpathbackend.entity.enums.PrescriptionStatus.PENDING_REVIEW and s.assignedPharmacist is null")
    int claim(@Param("id") Long id, @Param("pharmacistId") Long pharmacistId);

    // Customer order preview lookup by public code/pharmacy/customer
    @EntityGraph(attributePaths = {"items", "pharmacy", "prescription"})
    @Query("select s from PrescriptionSubmission s join s.prescription p where p.code = :code and s.pharmacy.id = :pharmacyId and p.customer.id = :customerId")
    Optional<PrescriptionSubmission> findForCustomerPreview(@Param("customerId") Long customerId,
                                                            @Param("code") String code,
                                                            @Param("pharmacyId") Long pharmacyId);
}
