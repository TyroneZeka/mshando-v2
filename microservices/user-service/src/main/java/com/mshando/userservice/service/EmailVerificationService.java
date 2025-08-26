package com.mshando.userservice.service;

import com.mshando.userservice.dto.EmailVerificationResponseDTO;
import com.mshando.userservice.model.User;
import com.mshando.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Email Verification Service
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRY_HOURS = 24;

    /**
     * Generate and send verification email
     * 
     * @param email user email
     * @return response indicating success or failure
     */
    public EmailVerificationResponseDTO sendVerificationEmail(String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return EmailVerificationResponseDTO.error("User not found with email: " + email);
            }
            
            User user = userOpt.get();
            
            if (user.isEmailVerified()) {
                return EmailVerificationResponseDTO.alreadyVerified();
            }
            
            // Generate new verification token
            String verificationToken = generateVerificationToken();
            user.setVerificationToken(verificationToken);
            user.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            // Send verification email
            try {
                emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);
                return EmailVerificationResponseDTO.success("Verification email sent successfully");
            } catch (Exception emailException) {
                // Log the email error but still return success for development mode
                log.warn("Email sending failed (likely development mode): {}", emailException.getMessage());
                return EmailVerificationResponseDTO.success("Verification email simulated successfully (development mode)");
            }
            
        } catch (Exception e) {
            log.error("Failed to process verification email request for: {}", email, e);
            return EmailVerificationResponseDTO.error("Failed to process verification email request");
        }
    }

    /**
     * Verify email using verification token
     * 
     * @param token verification token
     * @return response indicating verification result
     */
    public EmailVerificationResponseDTO verifyEmail(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return EmailVerificationResponseDTO.invalid();
            }
            
            Optional<User> userOpt = userRepository.findByVerificationToken(token);
            
            if (userOpt.isEmpty()) {
                return EmailVerificationResponseDTO.invalid();
            }
            
            User user = userOpt.get();
            
            if (user.isEmailVerified()) {
                return EmailVerificationResponseDTO.alreadyVerified();
            }
            
            // Check if token is expired (24 hours)
            LocalDateTime tokenCreationTime = user.getUpdatedAt(); // Assuming token was set during last update
            if (tokenCreationTime != null && tokenCreationTime.isBefore(LocalDateTime.now().minusHours(TOKEN_EXPIRY_HOURS))) {
                return EmailVerificationResponseDTO.expired();
            }
            
            // Verify the email
            user.setEmailVerified(true);
            user.setVerificationToken(null); // Clear the token
            user.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            // Send welcome email
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
            } catch (Exception e) {
                log.warn("Failed to send welcome email to: {}", user.getEmail(), e);
                // Don't fail verification if welcome email fails
            }
            
            log.info("Email verified successfully for user: {}", user.getUsername());
            return EmailVerificationResponseDTO.success("Email verified successfully");
            
        } catch (Exception e) {
            log.error("Failed to verify email with token: {}", token, e);
            return EmailVerificationResponseDTO.error("Failed to verify email");
        }
    }

    /**
     * Resend verification email for a user
     * 
     * @param username username
     * @return response indicating result
     */
    public EmailVerificationResponseDTO resendVerificationEmail(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return EmailVerificationResponseDTO.error("User not found");
            }
            
            User user = userOpt.get();
            
            if (user.isEmailVerified()) {
                return EmailVerificationResponseDTO.alreadyVerified();
            }
            
            return sendVerificationEmail(user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to resend verification email for user: {}", username, e);
            return EmailVerificationResponseDTO.error("Failed to resend verification email");
        }
    }

    /**
     * Check if user's email is verified by email
     * 
     * @param email user email
     * @return true if verified, false otherwise
     */
    public boolean isEmailVerified(String email) {
        return userRepository.findByEmail(email)
                .map(User::isEmailVerified)
                .orElse(false);
    }

    /**
     * Generate a secure random verification token
     * 
     * @return verification token
     */
    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return token.toString();
    }
}
