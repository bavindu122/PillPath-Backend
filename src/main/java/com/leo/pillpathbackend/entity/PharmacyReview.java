package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyReview {
    @Id
    @Column(name = "review_id", length = 36)
    private String reviewId; // UUID string

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "order_code", length = 64)
    private String orderCode;
}

