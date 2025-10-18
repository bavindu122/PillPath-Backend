package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByExternalKey(String externalKey);
    List<WalletTransaction> findByOrderCodeAndPharmacyOrderId(String orderCode, Long pharmacyOrderId);
}

