package com.mshando.paymentservice.model;

/**
 * Enumeration representing different types of payments in the system.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public enum PaymentType {
    /**
     * Payment for task completion
     */
    TASK_PAYMENT,
    
    /**
     * Platform service fee
     */
    SERVICE_FEE,
    
    /**
     * Refund for cancelled task
     */
    REFUND,
    
    /**
     * Deposit to platform balance
     */
    DEPOSIT,
    
    /**
     * Withdrawal from platform balance
     */
    WITHDRAWAL,
    
    /**
     * Penalty fee
     */
    PENALTY,
    
    /**
     * Bonus payment
     */
    BONUS
}
