//package com.leo.pillpathbackend.entity;
//
//import com.leo.pillpathbackend.entity.enums.OrderType;
//import  com.leo.pillpathbackend.entity.enums.OrderStatus;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import com.leo.pillpathbackend.entity.enums.*;
//import org.hibernate.annotations.UpdateTimestamp;
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "orders")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Order {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "order_number", unique = true)
//    private String orderNumber;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customer_id", nullable = false)
//    private Customer customer;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "pharmacy_id", nullable = false)
//    private Pharmacy pharmacy;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "prescription_id")
//    private Prescription prescription;
//
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "processed_by")
////    private Pharmacist processedBy;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "order_type")
//    private OrderType orderType;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status")
//    private OrderStatus status = OrderStatus.PENDING;
//
//    @Column(name = "total_amount", precision = 10, scale = 2)
//    private BigDecimal totalAmount;
//
//    @Column(name = "delivery_fee", precision = 10, scale = 2)
//    private BigDecimal deliveryFee = BigDecimal.ZERO;
//
//    @Column(name = "discount_amount", precision = 10, scale = 2)
//    private BigDecimal discountAmount = BigDecimal.ZERO;
//
//    @Column(name = "final_amount", precision = 10, scale = 2)
//    private BigDecimal finalAmount;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "payment_method")
//    private PaymentMethod paymentMethod;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "payment_status")
//    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
//
//    @Column(name = "delivery_address", columnDefinition = "TEXT")
//    private String deliveryAddress;
//
//    @Column(name = "delivery_notes", columnDefinition = "TEXT")
//    private String deliveryNotes;
//
//    @Column(name = "estimated_delivery_time")
//    private LocalDateTime estimatedDeliveryTime;
//
//    @Column(name = "actual_delivery_time")
//    private LocalDateTime actualDeliveryTime;
//
//    @CreationTimestamp
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    // Relationships
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<OrderItem> orderItems = new ArrayList<>();
//}
