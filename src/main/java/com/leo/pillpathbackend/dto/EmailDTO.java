package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDTO {
    private String to;
    private String toName;
    private String subject;
    private String preheader; // Email preview text
    private String content; // HTML content
    private String callToActionUrl;
    private String callToActionText;
    
    // Template variables
    private String recipientName;
    private String senderName;
    private String pharmacyName;
    private String prescriptionCode;
    private String orderCode;
    private String eventDescription;
    private String additionalInfo;
    private String reason; // For declined orders
}
