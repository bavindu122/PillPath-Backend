package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerProfilePicture;
    private Long pharmacistId;
    private String pharmacistName;
    private String pharmacistProfilePicture;
    private Long pharmacyId;
    private String pharmacyName;
    private String pharmacyLogoUrl;
    private Boolean isActive;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime createdAt;
}
