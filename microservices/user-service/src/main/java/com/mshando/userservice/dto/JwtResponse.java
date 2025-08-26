package com.mshando.userservice.dto;

/**
 * DTO for JWT authentication response
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private UserResponse user;

    // Default constructor
    public JwtResponse() {}

    // Constructor
    public JwtResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
