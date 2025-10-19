package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.WalletOwnerType;
import com.leo.pillpathbackend.entity.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_owner", columnList = "ownerType, ownerId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WalletOwnerType ownerType;

    @Column
    private Long ownerId; // null for PLATFORM

    @Column(nullable = false, length = 8)
    @Builder.Default
    private String currency = "LKR";

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

