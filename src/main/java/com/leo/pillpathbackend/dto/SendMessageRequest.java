package com.leo.pillpathbackend.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String text;
    private String sender; // optional: 'customer' | 'admin'
    private Long time; // optional epoch millis
}
