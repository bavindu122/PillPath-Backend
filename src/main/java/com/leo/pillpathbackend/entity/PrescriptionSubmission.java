package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.entity.enums.PrescriptionStatus;
import com.leo.pillpathbackend.entity.enums.SubmissionSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescription_submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_prescription_pharmacy", columnNames = {"prescription_id", "pharmacy_id"}))
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PrescriptionSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_pharmacist_id")
    private User assignedPharmacist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.PENDING_REVIEW;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionSubmissionItem> items = new ArrayList<>();

    // New: origin and references for reroute flows
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private SubmissionSource source = SubmissionSource.UPLOAD;

    @Column(name = "parent_preview_id")
    private String parentPreviewId; // preview code/id that this reroute came from

    @Column(name = "original_pharmacy_id")
    private Long originalPharmacyId; // the pharmacy that had unavailable items

    @Column(length = 1000, name = "note")
    private String note; // optional note for this submission (e.g., reroute note)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
