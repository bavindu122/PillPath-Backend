package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Wallet;
import com.leo.pillpathbackend.entity.enums.WalletOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByOwnerTypeAndOwnerId(WalletOwnerType ownerType, Long ownerId);
    Optional<Wallet> findByOwnerTypeAndOwnerIdIsNull(WalletOwnerType ownerType);
}
