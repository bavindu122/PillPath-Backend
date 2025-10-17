package com.leo.pillpathbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send a password reset email to the user.
     *
     * @param toEmail    The recipient email address
     * @param userName   The user's name
     * @param resetToken The password reset token
     * @param resetUrl   The complete reset URL
     * @throws MessagingException if email cannot be sent
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken, String resetUrl) throws MessagingException {
        log.info("Sending password reset email to: {}", toEmail);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Password Reset Request - PillPath");

        String htmlContent = buildPasswordResetEmail(userName, resetUrl, resetToken);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Password reset email sent successfully to: {}", toEmail);
    }

    /**
     * Build HTML email template for password reset.
     */
    private String buildPasswordResetEmail(String userName, String resetUrl, String token) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset - PillPath</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f7fa; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden;">
                                    
                                    <!-- Header with gradient -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                                                🔒 PillPath
                                            </h1>
                                            <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 14px; opacity: 0.9;">
                                                Your Health Journey Companion
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <h2 style="margin: 0 0 20px 0; color: #1a202c; font-size: 24px; font-weight: 600;">
                                                Password Reset Request
                                            </h2>
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Hello <strong>%s</strong>,
                                            </p>
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                We received a request to reset your password for your PillPath account. 
                                                Click the button below to create a new password:
                                            </p>
                                            
                                            <!-- Reset Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="%s" style="display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);">
                                                            Reset Password
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Alternative link -->
                                            <div style="margin: 30px 0; padding: 20px; background-color: #f7fafc; border-left: 4px solid #667eea; border-radius: 4px;">
                                                <p style="margin: 0 0 10px 0; color: #2d3748; font-size: 14px; font-weight: 600;">
                                                    Or copy and paste this link:
                                                </p>
                                                <p style="margin: 0; color: #667eea; font-size: 13px; word-break: break-all; font-family: 'Courier New', monospace;">
                                                    %s
                                                </p>
                                            </div>
                                            
                                            <!-- Security notice -->
                                            <div style="margin: 30px 0; padding: 20px; background-color: #fff5f5; border-left: 4px solid #fc8181; border-radius: 4px;">
                                                <p style="margin: 0 0 10px 0; color: #c53030; font-size: 14px; font-weight: 600;">
                                                    ⚠️ Important Security Notice
                                                </p>
                                                <ul style="margin: 0; padding-left: 20px; color: #742a2a; font-size: 13px; line-height: 1.6;">
                                                    <li>This link will expire in <strong>15 minutes</strong></li>
                                                    <li>If you didn't request this, please ignore this email</li>
                                                    <li>Your password will not change until you create a new one</li>
                                                    <li>Never share this link with anyone</li>
                                                </ul>
                                            </div>
                                            
                                            <p style="margin: 30px 0 0 0; color: #718096; font-size: 14px; line-height: 1.6;">
                                                Best regards,<br>
                                                <strong style="color: #667eea;">The PillPath Team</strong>
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px 0; color: #718096; font-size: 12px;">
                                                This is an automated email. Please do not reply.
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 11px;">
                                                © 2025 PillPath. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(userName, resetUrl, resetUrl);
    }

    /**
     * Send a password reset confirmation email.
     */
    public void sendPasswordResetConfirmationEmail(String toEmail, String userName) throws MessagingException {
        log.info("Sending password reset confirmation email to: {}", toEmail);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Password Successfully Reset - PillPath");

        String htmlContent = buildPasswordResetConfirmationEmail(userName);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Password reset confirmation email sent successfully to: {}", toEmail);
    }

    /**
     * Build HTML email template for password reset confirmation.
     */
    private String buildPasswordResetConfirmationEmail(String userName) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset Successful - PillPath</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f7fa; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #48bb78 0%%, #38a169 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700;">
                                                ✅ Password Reset Successful
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Hello <strong>%s</strong>,
                                            </p>
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Your password has been successfully reset. You can now sign in to your PillPath account with your new password.
                                            </p>
                                            
                                            <div style="margin: 30px 0; padding: 20px; background-color: #f0fff4; border-left: 4px solid #48bb78; border-radius: 4px;">
                                                <p style="margin: 0; color: #22543d; font-size: 14px;">
                                                    If you did not make this change or believe an unauthorized person accessed your account, 
                                                    please contact our support team immediately.
                                                </p>
                                            </div>
                                            
                                            <p style="margin: 30px 0 0 0; color: #718096; font-size: 14px; line-height: 1.6;">
                                                Best regards,<br>
                                                <strong style="color: #48bb78;">The PillPath Team</strong>
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px 0; color: #718096; font-size: 12px;">
                                                This is an automated email. Please do not reply.
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 11px;">
                                                © 2025 PillPath. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(userName);
    }
}
