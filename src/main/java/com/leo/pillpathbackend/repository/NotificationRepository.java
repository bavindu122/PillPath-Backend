package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a specific recipient
    List<Notification> findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
            Long recipientId, String recipientType);
    
    // Find unread notifications for a specific recipient
    List<Notification> findByRecipientIdAndRecipientTypeAndReadFalseOrderByCreatedAtDesc(
            Long recipientId, String recipientType);
    
    // Count unread notifications for a specific recipient
    long countByRecipientIdAndRecipientTypeAndReadFalse(Long recipientId, String recipientType);
    
    // Find notifications by prescription ID (to avoid duplicates)
    List<Notification> findByPrescriptionIdAndRecipientIdAndRecipientType(
            Long prescriptionId, Long recipientId, String recipientType);
    
    // Find notifications by order ID (to avoid duplicates)
    List<Notification> findByOrderIdAndRecipientIdAndRecipientType(
            Long orderId, Long recipientId, String recipientType);
}
