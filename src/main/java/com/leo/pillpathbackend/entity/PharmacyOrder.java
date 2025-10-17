// java
package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.PharmacyOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pharmacy_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_order_id")
    private CustomerOrder customerOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_submission_id")
    private PrescriptionSubmission submission;

    @Enumerated(EnumType.STRING)
    private PharmacyOrderStatus status = PharmacyOrderStatus.RECEIVED;

    @Column(length = 16)
    private String pickupCode;

    @Column(length = 64, unique = true)
    private String orderCode;

    @Column(length = 255)
    private String pickupLocation;

    private Double pickupLat;
    private Double pickupLng;

    @Column(columnDefinition = "TEXT")
    private String customerNote;

    @Column(columnDefinition = "TEXT")
    private String pharmacistNote;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal shipping = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pharmacyOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PharmacyOrderItem> items = new ArrayList<>();
}

