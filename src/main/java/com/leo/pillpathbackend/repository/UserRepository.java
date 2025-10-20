package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.entity.enums.AdminLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndPassword(String username, String password);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.adminLevel = :adminLevel")
    boolean existsByAdminLevel(@Param("adminLevel") AdminLevel adminLevel);

    // Type-based counts by subclass
    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = Customer")
    Long countCustomers();

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = PharmacyAdmin")
    Long countPharmacyAdmins();

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = Admin")
    Long countSystemAdmins();

    List<User> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT u FROM User u WHERE TYPE(u) = Customer")
    List<User> findAllCustomers();

    // Count users created within a date-time range
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Optional: existing native trend (not used by charts endpoint)
    @Query(value = """
        SELECT
            TO_CHAR(created_at, 'Mon') as month,
            COUNT(*) as users
        FROM users
        WHERE created_at >= CURRENT_DATE - INTERVAL '6 months'
        GROUP BY EXTRACT(MONTH FROM created_at), TO_CHAR(created_at, 'Mon')
        ORDER BY EXTRACT(MONTH FROM created_at)
        """, nativeQuery = true)
    List<Object[]> getUserRegistrationTrend();

    // Get all pharmacist IDs for a specific pharmacy
    @Query("SELECT p.id FROM PharmacistUser p WHERE p.pharmacy.id = :pharmacyId")
    List<Long> findPharmacistIdsByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = PharmacistUser")
    Long countPharmacists();

    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = Customer AND u.createdAt BETWEEN :start AND :end")
    long countCustomersByCreatedAtBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}