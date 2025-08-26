package com.mshando.paymentservice.model;

/**
 * Enumeration representing the various states of a payment in the system.
 * 
 * The payment lifecycle follows these transitions:
 * PENDING -> PROCESSING -> COMPLETED/FAILED
 * COMPLETED -> REFUND_PENDING -> REFUNDED
 * FAILED -> RETRY_PENDING -> PROCESSING
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public enum PaymentStatus {
    /**
     * Payment has been initiated but not yet processed
     */
    PENDING,
    
    /**
     * Payment is currently being processed by the payment provider
     */
    PROCESSING,
    
    /**
     * Payment has been successfully completed
     */
    COMPLETED,
    
    /**
     * Payment processing failed
     */
    FAILED,
    
    /**
     * Payment is being retried after a failure
     */
    RETRY_PENDING,
    
    /**
     * Refund has been initiated for this payment
     */
    REFUND_PENDING,
    
    /**
     * Payment has been successfully refunded
     */
    REFUNDED,
    
    /**
     * Refund processing failed
     */
    REFUND_FAILED,
    
    /**
     * Payment has been cancelled before processing
     */
    CANCELLED
}
