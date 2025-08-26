package com.mshando.biddingservice.exception;

/**
 * Exception thrown when a bid operation is not allowed due to business rules.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public class InvalidBidOperationException extends RuntimeException {
    
    public InvalidBidOperationException(String message) {
        super(message);
    }
    
    public InvalidBidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
