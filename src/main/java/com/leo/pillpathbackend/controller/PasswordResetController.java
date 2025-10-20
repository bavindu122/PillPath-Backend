package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.ForgotPasswordRequest;
import com.leo.pillpathbackend.dto.PasswordResetResponse;
import com.leo.pillpathbackend.dto.ResetPasswordRequest;
import com.leo.pillpathbackend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for password reset operations.
 */
@RestController
@RequestMapping("/api/v1/password-reset")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Request a password reset.
     * Sends an email with a reset link to the user.
     *
     * @param request Contains the user's email address
     * @return Response indicating success or failure
     */
    @PostMapping("/request")
    public ResponseEntity<PasswordResetResponse> requestPasswordReset(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());
        
        PasswordResetResponse response = passwordResetService.requestPasswordReset(request);
        
        // Always return 200 OK to prevent email enumeration attacks
        // Don't reveal whether the email exists in the system
        return ResponseEntity.ok(PasswordResetResponse.builder()
                .success(true)
                .message("If an account exists with this email, you will receive a password reset link shortly.")
                .build());
    }

    /**
     * Verify if a reset token is valid.
     *
     * @param token The reset token to verify
     * @return Response indicating if the token is valid
     */
    @GetMapping("/verify")
    public ResponseEntity<PasswordResetResponse> verifyResetToken(
            @RequestParam("token") String token) {
        log.info("Verifying reset token");
        
        PasswordResetResponse response = passwordResetService.verifyResetToken(token);
        
        return response.isSuccess() 
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Reset the password using a valid token.
     *
     * @param request Contains the token, new password, and password confirmation
     * @return Response indicating success or failure
     */
    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Attempting to reset password");
        
        PasswordResetResponse response = passwordResetService.resetPassword(request);
        
        return response.isSuccess() 
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
