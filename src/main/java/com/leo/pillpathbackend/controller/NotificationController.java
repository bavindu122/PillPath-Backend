package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.NotificationDTO;
import com.leo.pillpathbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Get all notifications for authenticated user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam Long userId,
            @RequestParam String userType) {
        
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId, userType);
        long unreadCount = notificationService.getUnreadCount(userId, userType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam Long userId,
            @RequestParam String userType) {
        
        notificationService.markAllAsRead(userId, userType);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam String userType) {
        
        long count = notificationService.getUnreadCount(userId, userType);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        
        return ResponseEntity.ok(response);
    }
}
