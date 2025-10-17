package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.ForgotPasswordRequest;
import com.leo.pillpathbackend.dto.PasswordResetResponse;
import com.leo.pillpathbackend.dto.ResetPasswordRequest;
import com.leo.pillpathbackend.entity.PasswordResetToken;
import com.leo.pillpathbackend.entity.User;
import com.leo.pillpathbackend.repository.PasswordResetTokenRepository;
import com.leo.pillpathbackend.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service for handling password reset operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-expiration-minutes:15}")
    private int tokenExpirationMinutes;

    @Value("${app.password-reset.base-url:http://localhost:5173}")
    private String baseUrl;

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Request a password reset for a user.
     * Generates a token and sends an email with reset link.
     */
    @Transactional
    public PasswordResetResponse requestPasswordReset(ForgotPasswordRequest request) {
        try {
            log.info("Password reset requested for email: {}", request.getEmail());

            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

            // Delete any existing tokens for this user
            tokenRepository.deleteByUser(user);

            // Generate new token
            String token = generateSecureToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);

            // Save token to database
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(expiryDate);
            resetToken.setUsed(false);
            tokenRepository.save(resetToken);

            // Build reset URL
            String resetUrl = baseUrl + "/reset-password?token=" + token;

            // Send email
            try {
                emailService.sendPasswordResetEmail(
                        user.getEmail(),
                        user.getFullName() != null ? user.getFullName() : user.getUsername(),
                        token,
                        resetUrl
                );
            } catch (MessagingException e) {
                log.error("Failed to send password reset email to: {}", user.getEmail(), e);
                throw new RuntimeException("Failed to send password reset email. Please try again later.");
            }

            log.info("Password reset email sent successfully to: {}", user.getEmail());

            return PasswordResetResponse.builder()
                    .success(true)
                    .message("Password reset link has been sent to your email address. Please check your inbox.")
                    .build();

        } catch (RuntimeException e) {
            log.error("Error processing password reset request", e);
            return PasswordResetResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Reset password using the token.
     */
    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        try {
            log.info("Attempting password reset with token");

            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return PasswordResetResponse.builder()
                        .success(false)
                        .message("Passwords do not match")
                        .build();
            }

            // Find token
            PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

            // Validate token
            if (!resetToken.isValid()) {
                if (resetToken.isExpired()) {
                    return PasswordResetResponse.builder()
                            .success(false)
                            .message("Reset token has expired. Please request a new password reset.")
                            .build();
                } else if (resetToken.getUsed()) {
                    return PasswordResetResponse.builder()
                            .success(false)
                            .message("Reset token has already been used. Please request a new password reset.")
                            .build();
                }
            }

            // Get user and update password
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Mark token as used
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);

            // Send confirmation email
            try {
                emailService.sendPasswordResetConfirmationEmail(
                        user.getEmail(),
                        user.getFullName() != null ? user.getFullName() : user.getUsername()
                );
            } catch (MessagingException e) {
                log.error("Failed to send password reset confirmation email", e);
                // Don't fail the operation if confirmation email fails
            }

            log.info("Password reset successful for user: {}", user.getEmail());

            return PasswordResetResponse.builder()
                    .success(true)
                    .message("Your password has been reset successfully. You can now login with your new password.")
                    .build();

        } catch (RuntimeException e) {
            log.error("Error resetting password", e);
            return PasswordResetResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Verify if a reset token is valid.
     */
    public PasswordResetResponse verifyResetToken(String token) {
        try {
            PasswordResetToken resetToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid reset token"));

            if (!resetToken.isValid()) {
                if (resetToken.isExpired()) {
                    return PasswordResetResponse.builder()
                            .success(false)
                            .message("Reset token has expired")
                            .build();
                } else if (resetToken.getUsed()) {
                    return PasswordResetResponse.builder()
                            .success(false)
                            .message("Reset token has already been used")
                            .build();
                }
            }

            return PasswordResetResponse.builder()
                    .success(true)
                    .message("Token is valid")
                    .build();

        } catch (RuntimeException e) {
            return PasswordResetResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Generate a secure random token for password reset.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Scheduled job to clean up expired and old used tokens.
     * Runs every day at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired password reset tokens");
        
        // Delete expired tokens
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        
        // Delete used tokens older than 7 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        tokenRepository.deleteOldUsedTokens(cutoffDate);
        
        log.info("Completed cleanup of expired password reset tokens");
    }
}
