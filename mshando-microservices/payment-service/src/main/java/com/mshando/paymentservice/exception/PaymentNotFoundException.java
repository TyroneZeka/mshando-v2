package com.mshando.paymentservice.exception;

/**
 * Exception thrown when a requested payment is not found.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
