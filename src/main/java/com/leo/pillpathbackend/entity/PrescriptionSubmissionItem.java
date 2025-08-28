package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prescription_submission_items")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PrescriptionSubmissionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private PrescriptionSubmission submission;

    private String medicineName;
    private String genericName;
    private String dosage;
    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;

    private Boolean available;

    @Column(length = 500)
    private String notes;
}

