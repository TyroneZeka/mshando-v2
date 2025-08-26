package com.mshando.userservice.controller;

import com.mshando.userservice.dto.LoginRequestDTO;
import com.mshando.userservice.dto.UserRegistrationRequestDTO;
import com.mshando.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication Controller for user registration and login
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user
     * 
     * @param registrationRequest user registration details
     * @return ResponseEntity with success message and user details
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Create a new user account with the provided information. Returns a JWT token upon successful registration.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration information",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserRegistrationRequestDTO.class),
                examples = @ExampleObject(
                    name = "Customer Registration",
                    summary = "Register as a customer",
                    value = """
                        {
                          "username": "johndoe",
                          "email": "john@example.com",
                          "password": "securePassword123",
                          "firstName": "John",
                          "lastName": "Doe"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Registration",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9...",
                          "tokenType": "Bearer",
                          "user": {
                            "id": 1,
                            "username": "johndoe",
                            "email": "john@example.com",
                            "firstName": "John",
                            "lastName": "Doe",
                            "role": "CUSTOMER",
                            "active": true,
                            "emailVerified": false
                          },
                          "message": "User registered successfully"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or user already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Registration Error",
                    value = """
                        {
                          "timestamp": "2025-08-20T10:30:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Username already exists",
                          "path": "/api/auth/register"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {
        try {
            var response = authService.register(registrationRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * User login
     * 
     * @param loginRequest login credentials
     * @return ResponseEntity with JWT token
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with username/email and password. Returns a JWT token for successful authentication.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User login credentials",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequestDTO.class),
                examples = @ExampleObject(
                    name = "Login Example",
                    summary = "Login with username and password",
                    value = """
                        {
                          "username": "johndoe",
                          "password": "securePassword123"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Login",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9...",
                          "tokenType": "Bearer",
                          "user": {
                            "id": 1,
                            "username": "johndoe",
                            "email": "john@example.com",
                            "firstName": "John",
                            "lastName": "Doe",
                            "role": "CUSTOMER",
                            "active": true,
                            "emailVerified": true
                          },
                          "message": "Login successful"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Login Error",
                    value = "Invalid username or password"
                )
            )
        )
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            var response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Validate JWT token
     * 
     * @param authorizationHeader Authorization header with Bearer token
     * @return ResponseEntity with token validation result
     */
    @GetMapping("/validate")
    @Operation(
        summary = "Validate JWT token",
        description = "Validate the provided JWT token and return user information if valid.",
        security = @SecurityRequirement(name = "bearerAuth"),
        parameters = @Parameter(
            name = "Authorization",
            description = "Bearer token for authentication",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token is valid",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Valid Token",
                    value = """
                        {
                          "valid": true,
                          "user": {
                            "id": 1,
                            "username": "johndoe",
                            "email": "john@example.com",
                            "role": "CUSTOMER",
                            "active": true,
                            "emailVerified": true
                          },
                          "message": "Token is valid"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Token",
                    value = "Token is invalid or expired"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Missing or malformed Authorization header"
        )
    })
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract token from "Bearer <token>" format
            String token = authorizationHeader;
            if (authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            var response = authService.validateToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Refresh JWT token
     * 
     * @param authorizationHeader Authorization header with Bearer token
     * @return ResponseEntity with new token
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT token",
        description = "Generate a new JWT token using the current valid token. Useful for extending session without re-authentication.",
        security = @SecurityRequirement(name = "bearerAuth"),
        parameters = @Parameter(
            name = "Authorization",
            description = "Current valid Bearer token",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Refreshed Token",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9...",
                          "tokenType": "Bearer",
                          "user": {
                            "id": 1,
                            "username": "johndoe",
                            "email": "john@example.com",
                            "role": "CUSTOMER",
                            "active": true,
                            "emailVerified": true
                          },
                          "message": "Token refreshed successfully"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired token",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    name = "Refresh Error",
                    value = "Token is invalid or expired"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Missing or malformed Authorization header"
        )
    })
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract token from "Bearer <token>" format
            String token = authorizationHeader;
            if (authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            var response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
