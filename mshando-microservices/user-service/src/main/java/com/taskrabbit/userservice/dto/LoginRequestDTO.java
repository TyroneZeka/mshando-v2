package com.taskrabbit.userservice.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTO
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public class LoginRequestDTO {
    
    @NotBlank(message = "Username or email is required")
    private String username; // Can be username or email

    @NotBlank(message = "Password is required")
    private String password;

    // Default constructor
    public LoginRequestDTO() {}

    // Constructor with all fields
    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "username='" + username + '\'' +
                '}';
    }
}
