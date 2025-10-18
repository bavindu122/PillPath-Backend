package com.leo.pillpathbackend.repository;

import com.leo.pillpathbackend.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    // Check if email was already sent for a specific event (duplicate prevention)
    Optional<EmailLog> findByRecipientEmailAndEmailTypeAndPrescriptionIdAndStatus(
        String recipientEmail, String emailType, Long prescriptionId, String status);
    
    Optional<EmailLog> findByRecipientEmailAndEmailTypeAndOrderIdAndStatus(
        String recipientEmail, String emailType, Long orderId, String status);
    
    // Get all emails for a specific prescription
    List<EmailLog> findByPrescriptionIdOrderBySentAtDesc(Long prescriptionId);
    
    // Get all emails for a specific order
    List<EmailLog> findByOrderIdOrderBySentAtDesc(Long orderId);
    
    // Get all emails sent to a recipient
    List<EmailLog> findByRecipientEmailOrderBySentAtDesc(String recipientEmail);
    
    // Count failed emails for monitoring
    Long countByStatusAndSentAtAfter(String status, LocalDateTime after);
}
