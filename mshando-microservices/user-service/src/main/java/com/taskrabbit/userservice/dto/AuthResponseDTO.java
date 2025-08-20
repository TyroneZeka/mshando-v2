package com.taskrabbit.userservice.dto;

/**
 * Authentication response DTO
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public class AuthResponseDTO {
    
    private String token;
    private String tokenType = "Bearer";
    private UserResponseDTO user;
    private String message;

    // Default constructor
    public AuthResponseDTO() {}

    // Constructor with all fields
    public AuthResponseDTO(String token, UserResponseDTO user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponseDTO getUser() {
        return user;
    }

    public void setUser(UserResponseDTO user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AuthResponseDTO{" +
                "token='" + token + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", user=" + user +
                ", message='" + message + '\'' +
                '}';
    }
}
