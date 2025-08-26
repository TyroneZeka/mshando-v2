package com.mshando.taskservice.exception;

/**
 * Exception thrown when a user tries to access a resource they're not authorized to access
 */
public class UnauthorizedAccessException extends RuntimeException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
