package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.CommissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_records", indexes = {
        @Index(name = "idx_commission_pharmacy_month", columnList = "pharmacyId, month"),
        @Index(name = "idx_commission_status", columnList = "status"),
        @Index(name = "idx_commission_order", columnList = "orderId, orderCode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Column(length = 64)
    private String orderCode;

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
    private CommissionStatus status = CommissionStatus.UNPAID;

    private LocalDateTime paidAt;

    @Column(length = 255)
    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

