package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Otc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtcRepository extends JpaRepository<Otc, Long> {
    
    @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId")
    List<Otc> findByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.addedToStore = true")
    List<Otc> findByPharmacyIdAndAddedToStoreTrue(@Param("pharmacyId") Long pharmacyId);

        // Find products by name (partial match, case-insensitive)
    @Query("SELECT o FROM Otc o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Otc> findByNameContainingIgnoreCase(@Param("productName") String productName);
}






















// package com.leo.pillpathbackend.repository;

// import com.leo.pillpathbackend.entity.Otc;

// // import scala.collection.immutable.List;
// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;

// public interface OtcRepository extends JpaRepository<Otc, Long> {
//     List<Otc> findByPharmacyId(Long pharmacyId);
    
//     // Find OTC products that are added to store for a specific pharmacy
//     List<Otc> findByPharmacyIdAndAddedToStoreTrue(Long pharmacyId);
// }