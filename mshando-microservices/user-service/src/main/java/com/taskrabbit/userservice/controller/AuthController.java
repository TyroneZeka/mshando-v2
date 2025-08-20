package com.taskrabbit.userservice.controller;

import com.taskrabbit.userservice.dto.LoginRequestDTO;
import com.taskrabbit.userservice.dto.UserRegistrationRequestDTO;
import com.taskrabbit.userservice.service.AuthService;
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
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
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
