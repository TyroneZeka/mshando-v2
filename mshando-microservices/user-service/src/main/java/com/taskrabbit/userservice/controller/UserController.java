package com.taskrabbit.userservice.controller;

import com.taskrabbit.userservice.dto.UserProfileUpdateDTO;
import com.taskrabbit.userservice.dto.UserResponseDTO;
import com.taskrabbit.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller for user management operations
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "User profile and account management endpoints")
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile
     * 
     * @param authHeader JWT token from Authorization header
     * @return current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Getting current user profile");
        UserResponseDTO user = userService.getCurrentUser(authHeader);
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user's profile
     * 
     * @param authHeader JWT token from Authorization header
     * @param updateRequest profile update data
     * @return updated user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserProfileUpdateDTO updateRequest) {
        
        log.info("Updating user profile");
        UserResponseDTO updatedUser = userService.updateProfile(authHeader, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Get user by ID (admin only)
     * 
     * @param userId user ID to retrieve
     * @return user details
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        log.info("Getting user by ID: {}", userId);
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Search users with pagination
     * 
     * @param query search query (optional)
     * @param pageable pagination parameters
     * @return page of users
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDTO>> searchUsers(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        
        log.info("Searching users with query: {}", query);
        Page<UserResponseDTO> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Delete user by ID (admin only)
     * 
     * @param userId user ID to delete
     * @return success response
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("Deleting user with ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
