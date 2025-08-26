package com.mshando.userservice.exception;

/**
 * Exception thrown when an authorization header is invalid
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public class InvalidAuthorizationException extends RuntimeException {
    
    public InvalidAuthorizationException(String message) {
        super(message);
    }
    
    public InvalidAuthorizationException() {
        super("Invalid authorization header. Expected format: Bearer <token>");
    }
}
