package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PrescriptionSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionSubmissionRepository extends JpaRepository<PrescriptionSubmission, Long> {
    List<PrescriptionSubmission> findByPrescriptionId(Long prescriptionId);

    List<PrescriptionSubmission> findByPrescriptionIdIn(List<Long> prescriptionIds);

    @EntityGraph(attributePaths = {"pharmacy"})
    List<PrescriptionSubmission> findWithPharmacyByPrescriptionIdIn(List<Long> prescriptionIds);
}

