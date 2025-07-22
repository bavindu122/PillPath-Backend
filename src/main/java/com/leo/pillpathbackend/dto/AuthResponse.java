package com.leo.pillpathbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String userType;
    private Long userId;
    private Long pharmacyId;
    private String email;
    private String fullName;
    private String token;
    private String redirectPath;
    private String adminLevel;
    private Object profile;
}
