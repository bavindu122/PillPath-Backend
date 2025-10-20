package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.CustomerOrderStatus;
import com.leo.pillpathbackend.entity.enums.PaymentMethod;
import com.leo.pillpathbackend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    // Optional: which family member this order is for
    @Column(name = "family_member_id")
    private Long familyMemberId;

    @Enumerated(EnumType.STRING)
    private CustomerOrderStatus status = CustomerOrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

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

    @Column(length = 8)
    private String currency = "LKR";

    // New field to store external payment reference / transaction id
    @Column(length = 128)
    private String paymentReference;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PharmacyOrder> pharmacyOrders = new ArrayList<>();
}