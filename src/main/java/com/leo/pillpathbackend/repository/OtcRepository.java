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






























// package com.leo.pillpathbackend.repository;

// import com.leo.pillpathbackend.entity.Otc;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.util.List;

// @Repository
// public interface OtcRepository extends JpaRepository<Otc, Long> {

//     // Find by pharmacy relationship (not pharmacyId)
//     List<Otc> findByPharmacyId(Long pharmacyId);
    
//     // Find by name containing (case insensitive)
//     List<Otc> findByNameContainingIgnoreCase(String name);
    
//     // ✅ FIXED: Use pharmacy.id instead of pharmacyId
//     @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND (o.stock = 0 OR o.stock <= 10) ORDER BY o.stock ASC, o.updatedAt DESC")
//     List<Otc> findStockAlertsByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
//     // ✅ FIXED: Use pharmacy.id instead of pharmacyId
//     @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.stock > 0 AND o.stock <= 10")
//     Long countLowStockByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
//     // ✅ FIXED: Use pharmacy.id and stock parameter
//     @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.stock = :stock")
//     Long countByPharmacyIdAndStock(@Param("pharmacyId") Long pharmacyId, @Param("stock") Integer stock);
// }


























// package com.leo.pillpathbackend.repository;

// import com.leo.pillpathbackend.entity.Otc;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.util.List;

// public interface OtcRepository extends JpaRepository<Otc, Long> {
    
//     @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId")
//     List<Otc> findByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
//     @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.addedToStore = true")
//     List<Otc> findByPharmacyIdAndAddedToStoreTrue(@Param("pharmacyId") Long pharmacyId);

//         // Find products by name (partial match, case-insensitive)
//     @Query("SELECT o FROM Otc o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
//     List<Otc> findByNameContainingIgnoreCase(@Param("productName") String productName);

//         // Find products with low stock (stock <= 10 and > 0)
//     List<Otc> findByPharmacyIdAndStockLessThanEqualAndStockGreaterThan(
//         Long pharmacyId, Integer maxStock, Integer minStock
//     );
    
//     // Find products that are out of stock (stock = 0)
//     List<Otc> findByPharmacyIdAndStock(Long pharmacyId, Integer stock);
    
//     // Find all products with stock alerts (low or out of stock)
//     @Query("SELECT o FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND (o.stock = 0 OR o.stock <= 10) ORDER BY o.stock ASC, o.updatedAt DESC")
//     List<Otc> findStockAlertsByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
//     // Count low stock items
//     @Query("SELECT COUNT(o) FROM Otc o WHERE o.pharmacy.id = :pharmacyId AND o.stock > 0 AND o.stock <= 10")
//     Long countLowStockByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
//     // Count out of stock items
//     Long countByPharmacyIdAndStock(Long pharmacyId, Integer stock);
// }






















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