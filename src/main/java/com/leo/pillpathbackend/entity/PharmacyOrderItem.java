package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import com.leo.pillpathbackend.entity.PharmacyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PharmacyOrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_order_id")
    private PharmacyOrder pharmacyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_item_id")
    private PrescriptionSubmissionItem submissionItem; // reference to preview

    @Column(nullable = false)
    private String medicineName;

    private String genericName;
    private String dosage;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

