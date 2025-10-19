package com.leo.pillpathbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModeratorCreateRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

