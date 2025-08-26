package com.mshando.paymentservice.model;

/**
 * Enumeration representing different payment methods supported by the system.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public enum PaymentMethod {
    /**
     * Credit card payment (Visa, Mastercard, etc.)
     */
    CREDIT_CARD,
    
    /**
     * Debit card payment
     */
    DEBIT_CARD,
    
    /**
     * PayPal payment
     */
    PAYPAL,
    
    /**
     * Stripe payment processing
     */
    STRIPE,
    
    /**
     * Bank transfer
     */
    BANK_TRANSFER,
    
    /**
     * Digital wallet (Apple Pay, Google Pay, etc.)
     */
    DIGITAL_WALLET,
    
    /**
     * Platform credit/balance
     */
    PLATFORM_CREDIT
}
