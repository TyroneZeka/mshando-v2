package com.mshando.userservice.dto;

import lombok.Data;

/**
 * DTO for email verification responses
 */
@Data
public class EmailVerificationResponseDTO {
    
    private boolean success;
    private String message;
    private String status; // VERIFIED, ALREADY_VERIFIED, EXPIRED, INVALID
    
    public EmailVerificationResponseDTO() {}
    
    public EmailVerificationResponseDTO(boolean success, String message, String status) {
        this.success = success;
        this.message = message;
        this.status = status;
    }
    
    public static EmailVerificationResponseDTO success(String message) {
        return new EmailVerificationResponseDTO(true, message, "VERIFIED");
    }
    
    public static EmailVerificationResponseDTO alreadyVerified() {
        return new EmailVerificationResponseDTO(true, "Email is already verified", "ALREADY_VERIFIED");
    }
    
    public static EmailVerificationResponseDTO expired() {
        return new EmailVerificationResponseDTO(false, "Verification token has expired", "EXPIRED");
    }
    
    public static EmailVerificationResponseDTO invalid() {
        return new EmailVerificationResponseDTO(false, "Invalid verification token", "INVALID");
    }
    
    public static EmailVerificationResponseDTO error(String message) {
        return new EmailVerificationResponseDTO(false, message, "ERROR");
    }
}
