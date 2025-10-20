package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wtxn_wallet", columnList = "wallet_id, createdAt"),
        @Index(name = "idx_wtxn_order", columnList = "orderCode"),
        @Index(name = "idx_wtxn_ext", columnList = "externalKey", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 48)
    private WalletTransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount; // signed

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(length = 64)
    private String orderCode;

    private Long pharmacyOrderId;
    private Long prescriptionId;

    @Column(length = 64)
    private String paymentId;

    @Column(length = 128, unique = true)
    private String externalKey; // idempotency key (nullable)

    @Column(length = 255)
    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

