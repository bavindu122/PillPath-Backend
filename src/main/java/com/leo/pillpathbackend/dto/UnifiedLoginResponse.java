// UnifiedLoginResponse.java
package com.leo.pillpathbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnifiedLoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String userType;  // "CUSTOMER", "PHARMACY_ADMIN", "PHARMACIST"
    private Long userId;
    private Object userProfile;  // Will contain the specific user profile DTO
}