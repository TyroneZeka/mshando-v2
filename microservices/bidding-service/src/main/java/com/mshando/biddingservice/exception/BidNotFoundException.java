package com.mshando.biddingservice.exception;

/**
 * Exception thrown when a requested bid is not found.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public class BidNotFoundException extends RuntimeException {
    
    public BidNotFoundException(String message) {
        super(message);
    }
    
    public BidNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static BidNotFoundException withId(Long bidId) {
        return new BidNotFoundException("Bid with ID " + bidId + " not found");
    }
}
