package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.PharmacyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PharmacyReviewRepository extends JpaRepository<PharmacyReview, String> {
    long countByPharmacyId(Long pharmacyId);

    @Query("select coalesce(avg(r.rating), 0) from PharmacyReview r where r.pharmacyId = :pid")
    Double averageRatingByPharmacy(@Param("pid") Long pharmacyId);

    Optional<PharmacyReview> findByCustomerIdAndPharmacyIdAndOrderCode(Long customerId, Long pharmacyId, String orderCode);

    // List reviews for a pharmacy newest first
    List<PharmacyReview> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
    List<PharmacyReview> findAllByOrderByCreatedAtDesc();
}
