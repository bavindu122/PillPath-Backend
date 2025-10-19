package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout_records", indexes = {
        @Index(name = "idx_payout_pharmacy_month", columnList = "pharmacyId, month"),
        @Index(name = "idx_payout_status", columnList = "status"),
        @Index(name = "idx_payout_order", columnList = "orderId, orderCode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId; // optional if monthly

    @Column(length = 64)
    private String orderCode; // optional if monthly

    private Long pharmacyId;

    @Column(length = 128)
    private String pharmacyName;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    // format MM/YYYY
    @Column(length = 16)
    private String month;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private PayoutStatus status = PayoutStatus.UNPAID;

    private LocalDateTime paidAt;

    @Column(length = 255)
    private String receiptUrl;

    @Column(length = 128)
    private String receiptFileName;

    @Column(length = 64)
    private String receiptFileType;

    @Column(length = 255)
    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

