package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.leo.pillpathbackend.entity.enums.AdminLevel;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndPassword(String username, String password);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.adminLevel = :adminLevel")
    boolean existsByAdminLevel(@Param("adminLevel") AdminLevel adminLevel);
}