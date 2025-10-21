package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Otc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtcRepository extends JpaRepository<Otc, Long> {

    // ✅ FIXED: Use @Query with pharmacy.id
    @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId")
    List<Otc> findByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
    // ✅ Find by name containing (case insensitive)
    List<Otc> findByNameContainingIgnoreCase(String name);
    
    // ✅ Find stock alerts (low stock and out of stock)
    @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND (o.stock = 0 OR o.stock <= 10) ORDER BY o.stock ASC, o.updatedAt DESC")
    List<Otc> findStockAlertsByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
    // ✅ Count low stock items
    @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.stock > 0 AND o.stock <= 10")
    Long countLowStockByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
    // ✅ Count items by pharmacy and stock
    @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.stock = :stock")
    Long countByPharmacyIdAndStock(@Param("pharmacyId") Long pharmacyId, @Param("stock") Integer stock);

        // Count total inventory items for a pharmacy
    @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId")
    Long countTotalInventoryByPharmacyId(@Param("pharmacyId") Long pharmacyId);


    // Count out of stock items
    @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.stock = 0")
    Long countOutOfStockByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}
