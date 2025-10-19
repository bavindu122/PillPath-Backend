package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response DTO for password reset operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    
    private boolean success;
    private String message;
    private Object data;
}
