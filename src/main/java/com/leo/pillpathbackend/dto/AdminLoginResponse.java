package com.leo.pillpathbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponse {
    private boolean success;
    private String message;
    private String token; // if using JWT
    private Long adminId;
    private String adminLevel;
}