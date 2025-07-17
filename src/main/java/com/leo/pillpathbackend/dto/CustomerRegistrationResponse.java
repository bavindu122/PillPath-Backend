package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String message;
    private boolean success;
}