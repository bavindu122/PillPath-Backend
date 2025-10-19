package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.NotificationDTO;
import com.leo.pillpathbackend.entity.Notification;
import com.leo.pillpathbackend.enums.NotificationType;
import com.leo.pillpathbackend.repository.NotificationRepository;
import com.leo.pillpathbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    /**
     * Get all notifications for a user
     */
    public List<NotificationDTO> getNotificationsForUser(Long userId, String userType) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(userId, userType);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unread count for a user
     */
    public long getUnreadCount(Long userId, String userType) {
        return notificationRepository
                .countByRecipientIdAndRecipientTypeAndReadFalse(userId, userType);
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId, String userType) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndRecipientTypeAndReadFalseOrderByCreatedAtDesc(userId, userType);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
    
    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Create notification for prescription sent to pharmacy
     * Notifies pharmacist(s) that a new prescription is awaiting review
     */
    @Transactional
    public void createPrescriptionSentNotification(
            Long prescriptionId,
            Long pharmacyId,
            List<Long> pharmacistIds,
            String customerName) {
        
        for (Long pharmacistId : pharmacistIds) {
            // Check for duplicates
            List<Notification> existing = notificationRepository
                    .findByPrescriptionIdAndRecipientIdAndRecipientType(
                            prescriptionId, pharmacistId, "PHARMACIST");
            
            if (existing.isEmpty()) {
                Notification notification = new Notification();
                notification.setTitle("New Prescription");
                notification.setMessage(String.format(
                        "New prescription from %s is awaiting review.", customerName));
                notification.setType(NotificationType.INFO);
                notification.setRecipientId(pharmacistId);
                notification.setRecipientType("PHARMACIST");
                notification.setPrescriptionId(prescriptionId);
                notification.setPharmacyId(pharmacyId);
                // Navigate pharmacists to their pharmacy review queue endpoint page
                // Backend endpoint pattern: GET /api/v1/prescriptions/pharmacy/{pharmacyId}
                // Include prescriptionId as query for client-side focus when present
                notification.setLink(String.format("/pharmacist/prescriptions/pharmacy/%d?prescriptionId=%d", pharmacyId, prescriptionId));
                notification.setCreatedAt(LocalDateTime.now());
                
                Notification savedNotification = notificationRepository.save(notification);
                log.info("Created prescription notification for pharmacist {}", pharmacistId);
                
                // Send email notification
                try {
                    userRepository.findById(pharmacistId).ifPresent(user -> {
                        String prescriptionCode = "RX-" + prescriptionId;
                        emailService.sendPrescriptionSentEmail(
                                user.getEmail(),
                                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                                customerName,
                                prescriptionId,
                                pharmacyId,
                                prescriptionCode,
                                LocalDateTime.now(),
                                savedNotification.getId()
                        );
                    });
                } catch (Exception e) {
                    log.error("Failed to send prescription email to pharmacist {}: {}", pharmacistId, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Create notification for order preview ready
     * Notifies customer that pharmacist has created an order preview
     */
    @Transactional
    public void createOrderPreviewReadyNotification(
            Long orderId,
            Long prescriptionId,
            Long customerId,
            String pharmacyName) {
        
        // Check for duplicates
        List<Notification> existing = notificationRepository
                .findByOrderIdAndRecipientIdAndRecipientType(
                        orderId, customerId, "CUSTOMER");
        
        if (existing.isEmpty()) {
            Notification notification = new Notification();
            notification.setTitle("Order Preview Ready");
            notification.setMessage(String.format(
                    "Order preview from %s is ready to review.", pharmacyName));
            notification.setType(NotificationType.INFO);
            notification.setRecipientId(customerId);
            notification.setRecipientType("CUSTOMER");
            notification.setOrderId(orderId);
            notification.setPrescriptionId(prescriptionId);
            notification.setLink(String.format("/customer/order-preview/%d", prescriptionId));
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Created order preview notification for customer {}", customerId);
            
            // Send email notification
            try {
                userRepository.findById(customerId).ifPresent(user -> {
                    String orderCode = "ORD-" + orderId;
                    emailService.sendOrderPreviewReadyEmail(
                            user.getEmail(),
                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                            pharmacyName,
                            orderId,
                            prescriptionId,
                            orderCode,
                            null, // estimated total - can be added later if available
                            savedNotification.getId()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to send order preview email to customer {}: {}", customerId, e.getMessage());
            }
        }
    }
    
    /**
     * Create notification for order confirmed
     * Notifies pharmacist(s) that customer has confirmed the order
     */
    @Transactional
    public void createOrderConfirmedNotification(
            Long orderId,
            Long pharmacyId,
            List<Long> pharmacistIds,
            String customerName) {
        
        for (Long pharmacistId : pharmacistIds) {
            Notification notification = new Notification();
            notification.setTitle("Order Confirmed");
            notification.setMessage(String.format(
                    "Customer %s confirmed the order. Proceed to preparation.", customerName));
            notification.setType(NotificationType.SUCCESS);
            notification.setRecipientId(pharmacistId);
            notification.setRecipientType("PHARMACIST");
            notification.setOrderId(orderId);
            notification.setPharmacyId(pharmacyId);
            notification.setLink(String.format("/pharmacist/orders/%d", orderId));
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Created order confirmed notification for pharmacist {}", pharmacistId);
            
            // Send email notification
            try {
                userRepository.findById(pharmacistId).ifPresent(user -> {
                    String orderCode = "ORD-" + orderId;
                    emailService.sendOrderConfirmedEmail(
                            user.getEmail(),
                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                            customerName,
                            orderId,
                            orderCode,
                            pharmacyId,
                            savedNotification.getId()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to send order confirmed email to pharmacist {}: {}", pharmacistId, e.getMessage());
            }
        }
    }
    
    /**
     * Create notification for order declined
     * Notifies pharmacist(s) that customer has declined the order preview
     */
    @Transactional
    public void createOrderDeclinedNotification(
            Long orderId,
            Long pharmacyId,
            List<Long> pharmacistIds,
            String customerName,
            String reason) {
        
        for (Long pharmacistId : pharmacistIds) {
            Notification notification = new Notification();
            notification.setTitle("Order Declined");
            String message = String.format(
                    "Customer %s declined the order preview.", customerName);
            if (reason != null && !reason.isEmpty()) {
                message += " Reason: " + reason;
            }
            notification.setMessage(message);
            notification.setType(NotificationType.WARNING);
            notification.setRecipientId(pharmacistId);
            notification.setRecipientType("PHARMACIST");
            notification.setOrderId(orderId);
            notification.setPharmacyId(pharmacyId);
            notification.setLink(String.format("/pharmacist/orders/%d", orderId));
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Created order declined notification for pharmacist {}", pharmacistId);
            
            // Send email notification
            try {
                userRepository.findById(pharmacistId).ifPresent(user -> {
                    String orderCode = "ORD-" + orderId;
                    emailService.sendOrderDeclinedEmail(
                            user.getEmail(),
                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                            customerName,
                            orderId,
                            orderCode,
                            reason,
                            pharmacyId,
                            savedNotification.getId()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to send order declined email to pharmacist {}: {}", pharmacistId, e.getMessage());
            }
        }
    }
    
    /**
     * Create notification for order ready
     * Notifies customer that their order is ready for pickup/delivery
     */
    @Transactional
    public void createOrderReadyNotification(
            Long orderId,
            String orderCode,
            Long customerId,
            String pharmacyName) {
        
        Notification notification = new Notification();
        notification.setTitle("Order Ready");
        notification.setMessage(String.format(
                "Your order from %s is ready for pickup.", pharmacyName));
        notification.setType(NotificationType.SUCCESS);
        notification.setRecipientId(customerId);
        notification.setRecipientType("CUSTOMER");
        notification.setOrderId(orderId);
        notification.setOrderCode(orderCode);
        notification.setLink(String.format("/customer/orders/%s", orderCode));
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created order ready notification for customer {}", customerId);
        
        // Send email notification
        try {
            userRepository.findById(customerId).ifPresent(user -> {
                String pickupCode = "PU-" + orderId; // Can be customized based on actual pickup code
                emailService.sendOrderReadyEmail(
                        user.getEmail(),
                        user.getFullName() != null ? user.getFullName() : user.getUsername(),
                        pharmacyName,
                        orderId,
                        orderCode,
                        pickupCode,
                        pharmacyName, // Location - can be enhanced with actual address
                        customerId,
                        savedNotification.getId()
                );
            });
        } catch (Exception e) {
            log.error("Failed to send order ready email to customer {}: {}", customerId, e.getMessage());
        }
    }
    
    /**
     * Create notification for order status changed to PREPARING
     * Notifies customer that pharmacist has started preparing their order
     */
    @Transactional
    public void createOrderPreparingNotification(
            Long orderId,
            String orderCode,
            Long customerId,
            String pharmacyName) {
        
        // Check for duplicates
        List<Notification> existing = notificationRepository
                .findByOrderIdAndRecipientIdAndRecipientType(
                        orderId, customerId, "CUSTOMER");
        
        // Check if PREPARING notification already exists
        boolean preparingExists = existing.stream()
                .anyMatch(n -> n.getTitle().equals("Order Being Prepared"));
        
        if (!preparingExists) {
            Notification notification = new Notification();
            notification.setTitle("Order Being Prepared");
            notification.setMessage(String.format(
                    "Your order from %s is now being prepared.", pharmacyName));
            notification.setType(NotificationType.INFO);
            notification.setRecipientId(customerId);
            notification.setRecipientType("CUSTOMER");
            notification.setOrderId(orderId);
            notification.setOrderCode(orderCode);
            notification.setLink(String.format("/customer/orders/%s", orderCode));
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Created order preparing notification for customer {}", customerId);
            
            // Send email notification
            try {
                userRepository.findById(customerId).ifPresent(user -> {
                    emailService.sendOrderPreparingEmail(
                            user.getEmail(),
                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                            pharmacyName,
                            orderId,
                            orderCode,
                            customerId,
                            savedNotification.getId()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to send order preparing email to customer {}: {}", customerId, e.getMessage());
            }
        }
    }
    
    /**
     * Create notification for order status changed to HANDED_OVER
     * Notifies customer that order has been collected/delivered
     */
    @Transactional
    public void createOrderHandedOverNotification(
            Long orderId,
            String orderCode,
            Long customerId,
            String pharmacyName,
            LocalDateTime handoverTime) {
        
        // Check for duplicates
        List<Notification> existing = notificationRepository
                .findByOrderIdAndRecipientIdAndRecipientType(
                        orderId, customerId, "CUSTOMER");
        
        // Check if HANDED_OVER notification already exists
        boolean handedOverExists = existing.stream()
                .anyMatch(n -> n.getTitle().equals("Order Collected"));
        
        if (!handedOverExists) {
            Notification notification = new Notification();
            notification.setTitle("Order Collected");
            notification.setMessage(String.format(
                    "Your order from %s has been successfully collected. Thank you for choosing us!", pharmacyName));
            notification.setType(NotificationType.SUCCESS);
            notification.setRecipientId(customerId);
            notification.setRecipientType("CUSTOMER");
            notification.setOrderId(orderId);
            notification.setOrderCode(orderCode);
            notification.setLink(String.format("/customer/orders/%s", orderCode));
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Created order handed over notification for customer {}", customerId);
            
            // Send email notification
            try {
                userRepository.findById(customerId).ifPresent(user -> {
                    emailService.sendOrderHandedOverEmail(
                            user.getEmail(),
                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                            pharmacyName,
                            orderId,
                            orderCode,
                            handoverTime,
                            customerId,
                            savedNotification.getId()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to send order handed over email to customer {}: {}", customerId, e.getMessage());
            }
        }
    }
    
    /**
     * Create notification for order cancelled by pharmacist
     * Notifies customer that pharmacist has cancelled their order
     */
    @Transactional
    public void createOrderCancelledByPharmacistNotification(
            Long orderId,
            Long customerId,
            String pharmacyName,
            String reason) {
        
        // Check for duplicates
        List<Notification> existing = notificationRepository
                .findByOrderIdAndRecipientIdAndRecipientType(orderId, customerId, "CUSTOMER");
        
        boolean alreadyNotified = existing.stream()
                .anyMatch(n -> "Order Cancelled".equals(n.getTitle()));
        
        if (!alreadyNotified) {
            Notification notification = new Notification();
            notification.setTitle("Order Cancelled");
            String message = String.format(
                    "Your order from %s has been cancelled by the pharmacy.", pharmacyName);
            if (reason != null && !reason.isEmpty()) {
                message += " Reason: " + reason;
            }
            notification.setMessage(message);
            notification.setType(NotificationType.WARNING);
            notification.setRecipientId(customerId);
            notification.setRecipientType("CUSTOMER");
            notification.setOrderId(orderId);
            notification.setLink(String.format("/customer/orders/%d", orderId));
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Created order cancelled notification for customer {}", customerId);
            
            // Send email notification
            try {
                userRepository.findById(customerId).ifPresent(user -> {
                    String orderCode = "ORD-" + orderId;
                    emailService.sendOrderCancelledEmail(
                            user.getEmail(),
                            user.getFullName() != null ? user.getFullName() : user.getUsername(),
                            pharmacyName,
                            orderId,
                            orderCode,
                            reason,
                            customerId,
                            savedNotification.getId()
                    );
                });
            } catch (Exception e) {
                log.error("Failed to send order cancelled email to customer {}: {}", customerId, e.getMessage());
            }
        }
    }
    
    /**
     * Convert entity to DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setLink(notification.getLink());
        dto.setOrderCode(notification.getOrderCode());
        dto.setPrescriptionId(notification.getPrescriptionId());
        dto.setOrderId(notification.getOrderId());
        dto.setPharmacyId(notification.getPharmacyId());
        dto.setCustomerId(notification.getCustomerId());
        return dto;
    }
}
