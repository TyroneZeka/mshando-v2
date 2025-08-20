package com.taskrabbit.userservice.controller;

import com.taskrabbit.userservice.dto.EmailVerificationRequestDTO;
import com.taskrabbit.userservice.dto.EmailVerificationResponseDTO;
import com.taskrabbit.userservice.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Email Verification Controller
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * Verify email using verification token
     * 
     * @param token verification token
     * @return verification response
     */
    @GetMapping("/verify-email")
    public ResponseEntity<EmailVerificationResponseDTO> verifyEmail(@RequestParam String token) {
        EmailVerificationResponseDTO response = emailVerificationService.verifyEmail(token);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Resend verification email
     * 
     * @param request email verification request
     * @return verification response
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<EmailVerificationResponseDTO> resendVerificationEmail(
            @Valid @RequestBody EmailVerificationRequestDTO request) {
        
        EmailVerificationResponseDTO response = emailVerificationService.sendVerificationEmail(request.getEmail());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Check email verification status
     * 
     * @param email email to check
     * @return verification status
     */
    @GetMapping("/verification-status")
    public ResponseEntity<?> checkVerificationStatus(@RequestParam String email) {
        try {
            boolean isVerified = emailVerificationService.isEmailVerified(email);
            return ResponseEntity.ok().body(new VerificationStatusResponse(email, isVerified));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to check verification status");
        }
    }

    /**
     * Inner class for verification status response
     */
    public static class VerificationStatusResponse {
        private String email;
        private boolean verified;

        public VerificationStatusResponse(String email, boolean verified) {
            this.email = email;
            this.verified = verified;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }
    }
}
