package com.mshando.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email Service for sending verification and notification emails
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@mshando.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String emailUsername;

    /**
     * Check if we're in development mode (no email credentials configured)
     */
    private boolean isDevelopmentMode() {
        return emailUsername == null || emailUsername.trim().isEmpty();
    }

    /**
     * Send email verification email
     * 
     * @param toEmail recipient email
     * @param username recipient username
     * @param verificationToken verification token
     */
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        try {
            // Check if we're in development mode (no real SMTP credentials)
            if (isDevelopmentMode()) {
                log.warn("=== DEVELOPMENT MODE: Email not configured ===");
                log.info("Verification email would be sent to: {}", toEmail);
                log.info("Verification token for {}: {}", toEmail, verificationToken);
                String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
                log.info("Verification URL: {}", verificationUrl);
                log.warn("=== END EMAIL SIMULATION ===");
                return; // Skip actual email sending in development
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Mshando Account");
            
            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Thank you for registering with Mshando!\n\n" +
                "Please click the link below to verify your email address:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't create this account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The Mshando Team",
                username, verificationUrl
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset email
     * 
     * @param toEmail recipient email
     * @param username recipient username
     * @param resetToken password reset token
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            // Check if we're in development mode (no real SMTP credentials)
            if (isDevelopmentMode()) {
                log.warn("=== DEVELOPMENT MODE: Password reset email not configured ===");
                log.info("Password reset email would be sent to: {}", toEmail);
                log.info("Reset token for {}: {}", toEmail, resetToken);
                String resetUrl = baseUrl + "/api/auth/reset-password?token=" + resetToken;
                log.info("Reset URL: {}", resetUrl);
                log.warn("=== END EMAIL SIMULATION ===");
                return; // Skip actual email sending in development
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset Your Mshando Password");
            
            String resetUrl = baseUrl + "/api/auth/reset-password?token=" + resetToken;
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "You have requested to reset your password for your Mshando account.\n\n" +
                "Please click the link below to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you didn't request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The Mshando Team",
                username, resetUrl
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send welcome email after verification
     * 
     * @param toEmail recipient email
     * @param username recipient username
     */
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Mshando!");
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Welcome to Mshando! Your email has been successfully verified.\n\n" +
                "You can now access all features of the platform:\n" +
                "- Browse and book services\n" +
                "- Connect with service providers\n" +
                "- Manage your profile and preferences\n\n" +
                "Visit your dashboard: %s/dashboard\n\n" +
                "Thank you for joining our community!\n\n" +
                "Best regards,\n" +
                "The Mshando Team",
                username, baseUrl
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception for welcome email failures
        }
    }
}
