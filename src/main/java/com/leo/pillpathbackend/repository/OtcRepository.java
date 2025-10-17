package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Otc;

// import scala.collection.immutable.List;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtcRepository extends JpaRepository<Otc, Long> {
    List<Otc> findByPharmacyId(Long pharmacyId);
    
    // Find OTC products that are added to store for a specific pharmacy
    List<Otc> findByPharmacyIdAndAddedToStoreTrue(Long pharmacyId);
}