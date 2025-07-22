//package com.leo.pillpathbackend.repository;
//
//import com.leo.pillpathbackend.entity.Pharmacist;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {
//    Optional<Pharmacist> findByEmail(String email);
//    Optional<Pharmacist> findByUsername(String username);
//    boolean existsByEmail(String email);
//    boolean existsByUsername(String username);
//}