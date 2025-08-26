package com.mshando.paymentservice.service;

import com.mshando.paymentservice.model.Payment;

/**
 * Service interface for payment provider integrations.
 * 
 * Handles communication with external payment providers
 * like Stripe, PayPal, etc.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public interface PaymentProviderService {
    
    /**
     * Process payment with external provider
     */
    String processPayment(Payment payment) throws Exception;
    
    /**
     * Process refund with external provider
     */
    String processRefund(Payment originalPayment, Payment refundPayment) throws Exception;
    
    /**
     * Check payment status with external provider
     */
    String checkPaymentStatus(String externalTransactionId) throws Exception;
    
    /**
     * Cancel payment with external provider
     */
    void cancelPayment(String externalTransactionId) throws Exception;
}
