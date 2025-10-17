package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.entity.enums.AdminLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // Dashboard specific queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.class = Customer")
    Long countCustomers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.class = PharmacyAdmin")
    Long countPharmacyAdmins();

    @Query("SELECT COUNT(u) FROM User u WHERE u.class = Admin")
    Long countSystemAdmins();

    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT u FROM User u WHERE u.class = Customer")
    List<User> findAllCustomers();

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
}