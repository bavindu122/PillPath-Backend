package com.leo.pillpathbackend.dto;

import lombok.Data;

@Data
public class UnifiedLoginRequest {
    private String email;
    private String password;
}