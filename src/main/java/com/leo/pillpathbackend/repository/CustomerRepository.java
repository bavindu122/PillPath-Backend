package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);
    List<Customer> findByInsuranceProvider(String insuranceProvider);
    List<Customer> findByPreferredPharmacyId(Long pharmacyId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}