package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.entity.EmailLog;
import com.leo.pillpathbackend.repository.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service for sending emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.mail.from-name:PillPath}")
    private String fromName;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
    
    // ========================================
    // PRESCRIPTION WORKFLOW NOTIFICATION EMAILS
    // ========================================
    
    /**
     * Send email for prescription sent to pharmacy
     */
    @Transactional
    public void sendPrescriptionSentEmail(
            String pharmacistEmail,
            String pharmacistName,
            String customerName,
            Long prescriptionId,
            Long pharmacyId,
            String prescriptionCode,
            LocalDateTime uploadedAt,
            Long notificationId) {
        
        if (!emailEnabled) {
            log.info("Email disabled - skipping prescription sent email");
            return;
        }
        
        // Check for duplicates
        if (isDuplicate(pharmacistEmail, "PRESCRIPTION_SENT", prescriptionId, null)) {
            log.info("Duplicate email prevented for pharmacist {} - prescription {}", pharmacistEmail, prescriptionId);
            return;
        }
        
        String subject = "New Prescription Awaiting Review";
        String preheader = String.format("New prescription from %s requires your attention", customerName);
        String htmlContent = buildPrescriptionSentEmail(
                pharmacistName, customerName, prescriptionCode, uploadedAt, prescriptionId);
        
        sendEmailWithLogging(pharmacistEmail, pharmacistName, subject, preheader, htmlContent,
                "PRESCRIPTION_SENT", prescriptionId, null, pharmacyId, null, null, notificationId);
    }
    
    /**
     * Send email for order preview ready
     */
    @Transactional
    public void sendOrderPreviewReadyEmail(
            String customerEmail,
            String customerName,
            String pharmacyName,
            Long orderId,
            Long prescriptionId,
            String orderCode,
            String estimatedTotal,
            Long notificationId) {
        
        if (!emailEnabled) {
            log.info("Email disabled - skipping order preview email");
            return;
        }
        
        // Check for duplicates
        if (isDuplicate(customerEmail, "ORDER_PREVIEW_READY", null, orderId)) {
            log.info("Duplicate email prevented for customer {} - order {}", customerEmail, orderId);
            return;
        }
        
        String subject = "Your Order Preview is Ready";
        String preheader = String.format("%s has prepared your order preview - review and confirm", pharmacyName);
        String htmlContent = buildOrderPreviewReadyEmail(
                customerName, pharmacyName, orderCode, estimatedTotal, orderId);
        
        sendEmailWithLogging(customerEmail, customerName, subject, preheader, htmlContent,
                "ORDER_PREVIEW_READY", prescriptionId, orderId, null, null, null, notificationId);
    }
    
    /**
     * Send email for order confirmed
     */
    @Transactional
    public void sendOrderConfirmedEmail(
            String pharmacistEmail,
            String pharmacistName,
            String customerName,
            Long orderId,
            String orderCode,
            Long pharmacyId,
            Long notificationId) {
        
        if (!emailEnabled) {
            log.info("Email disabled - skipping order confirmed email");
            return;
        }
        
        // Check for duplicates
        if (isDuplicate(pharmacistEmail, "ORDER_CONFIRMED", null, orderId)) {
            log.info("Duplicate email prevented for pharmacist {} - order {}", pharmacistEmail, orderId);
            return;
        }
        
        String subject = "Order Confirmed - Ready to Prepare";
        String preheader = String.format("%s confirmed their order - begin preparation", customerName);
        String htmlContent = buildOrderConfirmedEmail(
                pharmacistName, customerName, orderCode, orderId);
        
        sendEmailWithLogging(pharmacistEmail, pharmacistName, subject, preheader, htmlContent,
                "ORDER_CONFIRMED", null, orderId, pharmacyId, null, null, notificationId);
    }
    
    /**
     * Send email for order declined
     */
    @Transactional
    public void sendOrderDeclinedEmail(
            String pharmacistEmail,
            String pharmacistName,
            String customerName,
            Long orderId,
            String orderCode,
            String reason,
            Long pharmacyId,
            Long notificationId) {
        
        if (!emailEnabled) {
            log.info("Email disabled - skipping order declined email");
            return;
        }
        
        // Check for duplicates
        if (isDuplicate(pharmacistEmail, "ORDER_DECLINED", null, orderId)) {
            log.info("Duplicate email prevented for pharmacist {} - order {}", pharmacistEmail, orderId);
            return;
        }
        
        String subject = "Order Preview Declined";
        String preheader = String.format("%s declined the order preview", customerName);
        String htmlContent = buildOrderDeclinedEmail(
                pharmacistName, customerName, orderCode, reason, orderId);
        
        sendEmailWithLogging(pharmacistEmail, pharmacistName, subject, preheader, htmlContent,
                "ORDER_DECLINED", null, orderId, pharmacyId, null, null, notificationId);
    }
    
    /**
     * Send email for order ready
     */
    @Transactional
    public void sendOrderReadyEmail(
            String customerEmail,
            String customerName,
            String pharmacyName,
            Long orderId,
            String orderCode,
            String pickupCode,
            String pickupLocation,
            Long customerId,
            Long notificationId) {
        
        if (!emailEnabled) {
            log.info("Email disabled - skipping order ready email");
            return;
        }
        
        // Check for duplicates
        if (isDuplicate(customerEmail, "ORDER_READY", null, orderId)) {
            log.info("Duplicate email prevented for customer {} - order {}", customerEmail, orderId);
            return;
        }
        
        String subject = "Your Order is Ready for Pickup!";
        String preheader = String.format("Your order from %s is ready - pickup code: %s", pharmacyName, pickupCode);
        String htmlContent = buildOrderReadyEmail(
                customerName, pharmacyName, orderCode, pickupCode, pickupLocation, orderId);
        
        sendEmailWithLogging(customerEmail, customerName, subject, preheader, htmlContent,
                "ORDER_READY", null, orderId, null, customerId, null, notificationId);
    }
    
    /**
     * Check if email was already sent (duplicate prevention)
     */
    private boolean isDuplicate(String recipientEmail, String emailType, Long prescriptionId, Long orderId) {
        if (prescriptionId != null) {
            Optional<EmailLog> existing = emailLogRepository
                    .findByRecipientEmailAndEmailTypeAndPrescriptionIdAndStatus(
                            recipientEmail, emailType, prescriptionId, "SENT");
            return existing.isPresent();
        }
        
        if (orderId != null) {
            Optional<EmailLog> existing = emailLogRepository
                    .findByRecipientEmailAndEmailTypeAndOrderIdAndStatus(
                            recipientEmail, emailType, orderId, "SENT");
            return existing.isPresent();
        }
        
        return false;
    }
    
    /**
     * Core email sending method with logging
     */
    private void sendEmailWithLogging(
            String toEmail,
            String toName,
            String subject,
            String preheader,
            String htmlContent,
            String emailType,
            Long prescriptionId,
            Long orderId,
            Long pharmacyId,
            Long customerId,
            Long pharmacistId,
            Long notificationId) {
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Set preheader in email headers
            if (preheader != null) {
                message.setHeader("X-Entity-Ref-ID", preheader);
            }
            
            mailSender.send(message);
            
            // Log successful send
            logEmail(toEmail, toName, subject, emailType, "SENT", null,
                    prescriptionId, orderId, pharmacyId, customerId, pharmacistId, notificationId);
            
            log.info("Email sent successfully - Type: {}, To: {}, Subject: {}", emailType, toEmail, subject);
            
        } catch (MessagingException e) {
            log.error("Failed to send email - Type: {}, To: {}, Error: {}", emailType, toEmail, e.getMessage());
            logEmail(toEmail, toName, subject, emailType, "FAILED", e.getMessage(),
                    prescriptionId, orderId, pharmacyId, customerId, pharmacistId, notificationId);
        } catch (Exception e) {
            log.error("Unexpected error sending email - Type: {}, To: {}, Error: {}", emailType, toEmail, e.getMessage());
            logEmail(toEmail, toName, subject, emailType, "FAILED", e.getMessage(),
                    prescriptionId, orderId, pharmacyId, customerId, pharmacistId, notificationId);
        }
    }
    
    /**
     * Log email send attempt
     */
    private void logEmail(
            String recipientEmail,
            String recipientName,
            String subject,
            String emailType,
            String status,
            String errorMessage,
            Long prescriptionId,
            Long orderId,
            Long pharmacyId,
            Long customerId,
            Long pharmacistId,
            Long notificationId) {
        
        EmailLog emailLog = EmailLog.builder()
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .subject(subject)
                .emailType(emailType)
                .status(status)
                .errorMessage(errorMessage)
                .notificationId(notificationId)
                .prescriptionId(prescriptionId)
                .orderId(orderId)
                .pharmacyId(pharmacyId)
                .customerId(customerId)
                .pharmacistId(pharmacistId)
                .build();
        
        emailLogRepository.save(emailLog);
    }
    
    // ========================================
    // EMAIL TEMPLATES FOR NOTIFICATIONS
    // ========================================
    
    private String buildPrescriptionSentEmail(String pharmacistName, String customerName,
                                               String prescriptionCode, LocalDateTime uploadedAt, Long prescriptionId) {
        String reviewUrl = frontendUrl + "/pharmacist/prescriptions/" + prescriptionId;
        String uploadTime = uploadedAt.format(DATE_FORMATTER);
        
        return buildNotificationEmailTemplate(
                pharmacistName,
                "New Prescription Awaiting Review",
                String.format("A new prescription (Ref: <strong>%s</strong>) has been submitted by %s and requires your review.",
                        prescriptionCode, customerName),
                String.format("Received: %s", uploadTime),
                reviewUrl,
                "Review Prescription Now",
                "#667eea"
        );
    }
    
    private String buildOrderPreviewReadyEmail(String customerName, String pharmacyName,
                                                String orderCode, String estimatedTotal, Long orderId) {
        String previewUrl = frontendUrl + "/customer/orders/" + orderId + "/preview";
        String totalInfo = estimatedTotal != null ?
                String.format("Estimated Total: <strong>LKR %s</strong>", estimatedTotal) : "";
        
        return buildNotificationEmailTemplate(
                customerName,
                "Your Order Preview is Ready",
                String.format("%s has reviewed your prescription and prepared an order preview (Ref: <strong>%s</strong>).",
                        pharmacyName, orderCode),
                totalInfo,
                previewUrl,
                "Review Order Preview",
                "#48bb78"
        );
    }
    
    private String buildOrderConfirmedEmail(String pharmacistName, String customerName,
                                             String orderCode, Long orderId) {
        String orderUrl = frontendUrl + "/pharmacist/orders/" + orderId;
        
        return buildNotificationEmailTemplate(
                pharmacistName,
                "Order Confirmed - Ready to Prepare",
                String.format("%s has confirmed the order (Ref: <strong>%s</strong>) and completed payment.",
                        customerName, orderCode),
                "Please proceed with order preparation.",
                orderUrl,
                "View Order Details",
                "#667eea"
        );
    }
    
    private String buildOrderDeclinedEmail(String pharmacistName, String customerName,
                                            String orderCode, String reason, Long orderId) {
        String orderUrl = frontendUrl + "/pharmacist/orders/" + orderId;
        String reasonInfo = (reason != null && !reason.isEmpty()) ?
                String.format("Reason: <em>%s</em>", reason) : "No reason provided";
        
        return buildNotificationEmailTemplate(
                pharmacistName,
                "Order Preview Declined",
                String.format("%s has declined the order preview (Ref: <strong>%s</strong>).",
                        customerName, orderCode),
                reasonInfo,
                orderUrl,
                "View Order Details",
                "#fc8181"
        );
    }
    
    private String buildOrderReadyEmail(String customerName, String pharmacyName, String orderCode,
                                         String pickupCode, String pickupLocation, Long orderId) {
        String orderUrl = frontendUrl + "/customer/orders/" + orderId;
        String pickupInfo = String.format(
                "Pickup Code: <strong>%s</strong><br>Location: %s",
                pickupCode, pickupLocation);
        
        return buildNotificationEmailTemplate(
                customerName,
                "Your Order is Ready for Pickup!",
                String.format("Great news! Your order from %s (Ref: <strong>%s</strong>) is ready for pickup.",
                        pharmacyName, orderCode),
                pickupInfo,
                orderUrl,
                "View Pickup Details",
                "#48bb78"
        );
    }
    
    /**
     * Generic notification email template
     */
    private String buildNotificationEmailTemplate(String recipientName, String heading,
                                                    String eventDescription, String additionalInfo,
                                                    String ctaUrl, String ctaText, String accentColor) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - PillPath</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f7fa; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                                💊 PillPath
                                            </h1>
                                            <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 14px; opacity: 0.9;">
                                                Your Health Journey Companion
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <h2 style="margin: 0 0 20px 0; color: #1a202c; font-size: 22px; font-weight: 600;">
                                                %s
                                            </h2>
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Hello <strong>%s</strong>,
                                            </p>
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                %s
                                            </p>
                                            
                                            <!-- Additional Info -->
                                            %s
                                            
                                            <!-- CTA Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="%s" style="display: inline-block; padding: 16px 40px; background: %s; color: #ffffff; text-decoration: none; border-radius: 8px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);">
                                                            %s
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Alternative link -->
                                            <div style="margin: 30px 0; padding: 15px; background-color: #f7fafc; border-radius: 4px; text-align: center;">
                                                <p style="margin: 0 0 5px 0; color: #718096; font-size: 12px;">
                                                    Or copy and paste this link:
                                                </p>
                                                <p style="margin: 0; color: %s; font-size: 12px; word-break: break-all; font-family: 'Courier New', monospace;">
                                                    %s
                                                </p>
                                            </div>
                                            
                                            <p style="margin: 30px 0 0 0; color: #718096; font-size: 14px; line-height: 1.6;">
                                                Best regards,<br>
                                                <strong style="color: %s;">The PillPath Team</strong>
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f7fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px 0; color: #718096; font-size: 12px;">
                                                This is an automated email notification. You received this because of activity in your PillPath account.
                                            </p>
                                            <p style="margin: 0; color: #a0aec0; font-size: 11px;">
                                                © %d PillPath. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                heading,  // Title
                accentColor, accentColor,  // Gradient colors
                heading,  // H2 heading
                recipientName,  // Greeting
                eventDescription,  // Main message
                additionalInfo != null && !additionalInfo.isEmpty() ?
                        String.format("<div style=\"margin: 20px 0; padding: 15px; background-color: #f7fafc; border-left: 4px solid %s; border-radius: 4px;\"><p style=\"margin: 0; color: #2d3748; font-size: 14px;\">%s</p></div>",
                                accentColor, additionalInfo) : "",  // Additional info box
                ctaUrl,  // CTA URL
                accentColor,  // Button color
                ctaText,  // Button text
                accentColor,  // Alternative link color
                ctaUrl,  // Alternative link URL
                accentColor,  // Signature color
                LocalDateTime.now().getYear()  // Copyright year
        );
    }
    
    // ========================================
    // PASSWORD RESET EMAILS (EXISTING)
    // ========================================

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
