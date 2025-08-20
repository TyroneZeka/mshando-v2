package com.taskrabbit.userservice.dto;

/**
 * Token validation response DTO
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public class TokenValidationResponseDTO {
    
    private boolean valid;
    private String username;
    private String role;
    private String message;

    // Default constructor
    public TokenValidationResponseDTO() {}

    // Constructor with all fields
    public TokenValidationResponseDTO(boolean valid, String username, String role, String message) {
        this.valid = valid;
        this.username = username;
        this.role = role;
        this.message = message;
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TokenValidationResponseDTO{" +
                "valid=" + valid +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
