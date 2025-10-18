package com.leo.pillpathbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to track all email sends for auditing and preventing duplicates
 */
@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Column(name = "subject", nullable = false)
    private String subject;
    
    @Column(name = "email_type", nullable = false)
    private String emailType; // PRESCRIPTION_SENT, ORDER_PREVIEW_READY, ORDER_CONFIRMED, ORDER_DECLINED, ORDER_READY
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "status", nullable = false)
    private String status; // SENT, FAILED, BOUNCED
    
    // Reference to related entities for traceability
    @Column(name = "notification_id")
    private Long notificationId;
    
    @Column(name = "prescription_id")
    private Long prescriptionId;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "pharmacy_id")
    private Long pharmacyId;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "pharmacist_id")
    private Long pharmacistId;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
