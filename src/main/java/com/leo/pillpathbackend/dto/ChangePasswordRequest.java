package com.leo.pillpathbackend.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String username;
    private String currentPassword;
    private String newPassword;
}