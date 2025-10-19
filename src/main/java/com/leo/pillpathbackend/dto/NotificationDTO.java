package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
    private String link;
    private Long prescriptionId;
    private Long orderId;
    private String orderCode; // e.g., "ORD-20251019-1"
    private Long pharmacyId;
    private Long customerId;
}
