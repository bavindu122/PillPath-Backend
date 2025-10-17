package com.leo.pillpathbackend.entity;

import com.leo.pillpathbackend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private boolean read = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Link to the relevant resource (e.g., /customer/prescription/123)
    @Column(name = "link_url")
    private String link;
    
    // Recipient information
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
    
    @Column(name = "recipient_type", nullable = false)
    private String recipientType; // "CUSTOMER", "PHARMACIST", "PHARMACY"
    
    // Reference information
    @Column(name = "prescription_id")
    private Long prescriptionId;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "pharmacy_id")
    private Long pharmacyId;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
