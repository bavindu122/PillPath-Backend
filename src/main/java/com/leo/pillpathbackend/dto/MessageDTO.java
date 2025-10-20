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
public class MessageDTO {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderName;
    private String senderProfilePicture;
    private String senderType;
    private String content;
    private String messageType;
    private String mediaUrl;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime timestamp;
}

