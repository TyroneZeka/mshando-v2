package com.mshando.userservice.controller;

import com.mshando.userservice.dto.EmailVerificationRequestDTO;
import com.mshando.userservice.dto.EmailVerificationResponseDTO;
import com.mshando.userservice.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "Email verification and management endpoints")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * Verify email using verification token
     * 
     * @param token verification token
     * @return verification response
     */
    @GetMapping("/verify-email")
    @Operation(
        summary = "Verify email address",
        description = "Verify user's email address using the verification token sent via email.",
        parameters = @Parameter(
            name = "token",
            description = "Verification token received via email",
            required = true,
            example = "abc123def456ghi789jkl012mno345pq"
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EmailVerificationResponseDTO.class),
                examples = @ExampleObject(
                    name = "Verification Success",
                    value = """
                        {
                          "success": true,
                          "message": "Email verified successfully",
                          "status": "SUCCESS"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid, expired, or already used token",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Invalid Token",
                        summary = "Token is invalid",
                        value = """
                            {
                              "success": false,
                              "message": "Invalid verification token",
                              "status": "INVALID"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Expired Token",
                        summary = "Token has expired",
                        value = """
                            {
                              "success": false,
                              "message": "Verification token has expired",
                              "status": "EXPIRED"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Already Verified",
                        summary = "Email already verified",
                        value = """
                            {
                              "success": false,
                              "message": "Email is already verified",
                              "status": "ALREADY_VERIFIED"
                            }
                            """
                    )
                }
            )
        )
    })
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
    @Operation(
        summary = "Resend verification email",
        description = "Send a new verification email to the user's email address.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email address to send verification to",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EmailVerificationRequestDTO.class),
                examples = @ExampleObject(
                    name = "Resend Request",
                    value = """
                        {
                          "email": "user@example.com"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verification email sent successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Email Sent",
                    value = """
                        {
                          "success": true,
                          "message": "Verification email sent successfully",
                          "status": "SUCCESS"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid email or user not found",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "User Not Found",
                        value = """
                            {
                              "success": false,
                              "message": "User not found with email: user@example.com",
                              "status": "ERROR"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Already Verified",
                        value = """
                            {
                              "success": false,
                              "message": "Email is already verified",
                              "status": "ALREADY_VERIFIED"
                            }
                            """
                    )
                }
            )
        )
    })
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
    @Operation(
        summary = "Check email verification status",
        description = "Check whether a user's email address has been verified.",
        parameters = @Parameter(
            name = "email",
            description = "Email address to check verification status for",
            required = true,
            example = "user@example.com"
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verification status retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Verified Email",
                        summary = "Email is verified",
                        value = """
                            {
                              "email": "user@example.com",
                              "verified": true
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Unverified Email",
                        summary = "Email is not verified",
                        value = """
                            {
                              "email": "user@example.com",
                              "verified": false
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Failed to check verification status",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    name = "Check Error",
                    value = "Failed to check verification status"
                )
            )
        )
    })
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
