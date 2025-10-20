package com.leo.pillpathbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    @NotBlank
    private String provider; // expected: "google"
    @NotBlank
    private String idToken;
}
