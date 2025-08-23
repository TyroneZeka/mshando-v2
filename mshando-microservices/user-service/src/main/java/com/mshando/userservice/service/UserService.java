package com.mshando.userservice.service;

import com.mshando.userservice.dto.UserProfileUpdateDTO;
import com.mshando.userservice.dto.UserResponseDTO;
import com.mshando.userservice.exception.InvalidAuthorizationException;
import com.mshando.userservice.exception.UserNotFoundException;
import com.mshando.userservice.model.User;
import com.mshando.userservice.model.Profile;
import com.mshando.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service for user management operations
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Get current user from JWT token
     */
    public UserResponseDTO getCurrentUser(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        String username = jwtService.extractUsername(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("username", username));
        
        return mapToUserResponseDTO(user);
    }

    /**
     * Update user profile
     */
    public UserResponseDTO updateProfile(String authHeader, UserProfileUpdateDTO updateRequest) {
        String token = extractTokenFromHeader(authHeader);
        String username = jwtService.extractUsername(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("username", username));
        
        // Update user basic fields
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        
        // Get or create profile
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile(user);
            profile.setCreatedAt(java.time.LocalDateTime.now());
            profile.setUpdatedAt(java.time.LocalDateTime.now());
            user.setProfile(profile);
        } else {
            profile.setUpdatedAt(java.time.LocalDateTime.now());
        }
        
        // Update profile fields
        if (updateRequest.getBio() != null) {
            profile.setBio(updateRequest.getBio());
        }
        if (updateRequest.getAddress() != null) {
            profile.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getCity() != null) {
            profile.setCity(updateRequest.getCity());
        }
        if (updateRequest.getState() != null) {
            profile.setState(updateRequest.getState());
        }
        if (updateRequest.getPostalCode() != null) {
            profile.setPostalCode(updateRequest.getPostalCode());
        }
        if (updateRequest.getCountry() != null) {
            profile.setCountry(updateRequest.getCountry());
        }
        
        User updatedUser = userRepository.save(user);
        return mapToUserResponseDTO(updatedUser);
    }

    /**
     * Get user by ID with authorization check
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId, String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        String currentRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Long currentUserId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));

        // Check if user is admin or accessing their own profile
        if (!"ADMIN".equals(currentRole) && !currentUserId.equals(userId)) {
            throw new SecurityException("Access denied: You can only access your own profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return mapToUserResponseDTO(user);
    }

    /**
     * Search users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(String query, Pageable pageable) {
        Page<User> users;
        
        if (query == null || query.trim().isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    query, query, pageable);
        }
        
        return users.map(this::mapToUserResponseDTO);
    }

    /**
     * Delete user by ID (admin only)
     */
    public void deleteUser(Long userId, String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        String currentRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Only admins can delete users
        if (!"ADMIN".equals(currentRole)) {
            throw new SecurityException("Access denied: Only admins can delete users");
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        
        userRepository.deleteById(userId);
        log.info("User deleted successfully with ID: {}", userId);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationException();
        }
        return authHeader.substring(7);
    }

    /**
     * Map User entity to UserResponseDTO
     */
    private UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole().toString(),
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }
}
