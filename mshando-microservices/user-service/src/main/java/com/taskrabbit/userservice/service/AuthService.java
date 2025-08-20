package com.taskrabbit.userservice.service;

import com.taskrabbit.userservice.dto.AuthResponseDTO;
import com.taskrabbit.userservice.dto.LoginRequestDTO;
import com.taskrabbit.userservice.dto.TokenValidationResponseDTO;
import com.taskrabbit.userservice.dto.UserRegistrationRequestDTO;
import com.taskrabbit.userservice.dto.UserResponseDTO;
import com.taskrabbit.userservice.model.Role;
import com.taskrabbit.userservice.model.User;
import com.taskrabbit.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public AuthResponseDTO register(UserRegistrationRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        
        // Send verification email
        try {
            emailVerificationService.sendVerificationEmail(savedUser.getEmail());
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
        
        String accessToken = jwtService.generateToken(savedUser);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(accessToken);
        response.setTokenType("Bearer");
        response.setUser(convertToUserResponseDTO(savedUser));
        response.setMessage("User registered successfully");

        return response;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is not active");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(accessToken);
        response.setTokenType("Bearer");
        response.setUser(convertToUserResponseDTO(user));
        response.setMessage("Login successful");

        return response;
    }

    public TokenValidationResponseDTO validateToken(String token) {
        try {
            // Debug: Log the token being validated
            System.out.println("DEBUG: Validating token: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            String username = jwtService.extractUsername(token);
            System.out.println("DEBUG: Extracted username: " + username);
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("DEBUG: Found user: " + user.getUsername() + ", Active: " + user.isActive() + ", Verified: " + user.isEmailVerified());
            
            // Check individual components of validation
            String tokenUsername = jwtService.extractUsername(token);
            boolean usernameMatches = tokenUsername.equals(user.getUsername());
            
            System.out.println("DEBUG: Token username: " + tokenUsername + ", User username: " + user.getUsername());
            System.out.println("DEBUG: Username matches: " + usernameMatches);
            System.out.println("DEBUG: Current time: " + new Date());

            boolean isValid = jwtService.isTokenValid(token, user);
            System.out.println("DEBUG: Token valid: " + isValid);

            TokenValidationResponseDTO response = new TokenValidationResponseDTO();
            response.setValid(isValid);
            response.setUsername(username);
            response.setRole(user.getRole().toString());

            return response;
        } catch (Exception e) {
            // Log the actual exception
            System.err.println("DEBUG: Token validation failed with exception: " + e.getMessage());
            e.printStackTrace();
            
            TokenValidationResponseDTO response = new TokenValidationResponseDTO();
            response.setValid(false);
            response.setMessage("Token validation failed: " + e.getMessage());
            return response;
        }
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new RuntimeException("Invalid refresh token");
            }

            String newAccessToken = jwtService.generateToken(user);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(newAccessToken);
            response.setTokenType("Bearer");
            response.setUser(convertToUserResponseDTO(user));
            response.setMessage("Token refreshed successfully");

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole() != null ? user.getRole().toString() : "CUSTOMER");
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }
}
