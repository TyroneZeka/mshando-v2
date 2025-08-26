package com.mshando.paymentservice.exception;

/**
 * Exception thrown when an invalid payment operation is attempted.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public class InvalidPaymentOperationException extends RuntimeException {
    
    public InvalidPaymentOperationException(String message) {
        super(message);
    }
    
    public InvalidPaymentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
