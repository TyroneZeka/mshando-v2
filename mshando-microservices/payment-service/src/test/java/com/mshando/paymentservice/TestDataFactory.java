package com.mshando.paymentservice;

import com.mshando.paymentservice.model.Payment;
import com.mshando.paymentservice.model.PaymentMethod;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory class for creating test data objects.
 * 
 * Provides convenient methods to create Payment entities
 * and DTOs for testing purposes.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public class TestDataFactory {
    
    /**
     * Create a basic payment for testing
     */
    public static Payment createPayment() {
        return Payment.builder()
                .customerId(1L)
                .taskerId(2L)
                .taskId(3L)
                .bidId(4L)
                .amount(BigDecimal.valueOf(100.00))
                .serviceFee(BigDecimal.valueOf(10.00))
                .netAmount(BigDecimal.valueOf(90.00))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentType(PaymentType.TASK_PAYMENT)
                .status(PaymentStatus.PENDING)
                .description("Test payment for task completion")
                .retryCount(0)
                .maxRetries(3)
                .build();
    }
    
    /**
     * Create a payment with custom amount
     */
    public static Payment createPaymentWithAmount(BigDecimal amount) {
        Payment payment = createPayment();
        payment.setAmount(amount);
        payment.calculateServiceFee(BigDecimal.valueOf(10)); // 10% service fee
        return payment;
    }
    
    /**
     * Create a payment with specific status
     */
    public static Payment createPaymentWithStatus(PaymentStatus status) {
        Payment payment = createPayment();
        payment.setStatus(status);
        
        // Set appropriate timestamps based on status
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case PROCESSING:
                payment.setProcessedAt(now);
                break;
            case COMPLETED:
                payment.setProcessedAt(now.minusMinutes(5));
                payment.setCompletedAt(now);
                break;
            case FAILED:
                payment.setProcessedAt(now.minusMinutes(5));
                payment.setFailedAt(now);
                payment.setFailureReason("Test failure reason");
                break;
            case REFUNDED:
                payment.setProcessedAt(now.minusMinutes(10));
                payment.setCompletedAt(now.minusMinutes(5));
                payment.setRefundedAt(now);
                break;
        }
        
        return payment;
    }
    
    /**
     * Create a payment for specific customer
     */
    public static Payment createPaymentForCustomer(Long customerId) {
        Payment payment = createPayment();
        payment.setCustomerId(customerId);
        return payment;
    }
    
    /**
     * Create a payment for specific tasker
     */
    public static Payment createPaymentForTasker(Long taskerId) {
        Payment payment = createPayment();
        payment.setTaskerId(taskerId);
        return payment;
    }
    
    /**
     * Create a payment for specific task
     */
    public static Payment createPaymentForTask(Long taskId) {
        Payment payment = createPayment();
        payment.setTaskId(taskId);
        return payment;
    }
    
    /**
     * Create a payment with specific payment method
     */
    public static Payment createPaymentWithMethod(PaymentMethod method) {
        Payment payment = createPayment();
        payment.setPaymentMethod(method);
        return payment;
    }
    
    /**
     * Create a payment with specific payment type
     */
    public static Payment createPaymentWithType(PaymentType type) {
        Payment payment = createPayment();
        payment.setPaymentType(type);
        return payment;
    }
    
    /**
     * Create a failed payment that can be retried
     */
    public static Payment createRetriablePayment() {
        Payment payment = createPaymentWithStatus(PaymentStatus.FAILED);
        payment.setRetryCount(1);
        payment.setMaxRetries(3);
        return payment;
    }
    
    /**
     * Create a failed payment that has exceeded max retries
     */
    public static Payment createMaxRetriedPayment() {
        Payment payment = createPaymentWithStatus(PaymentStatus.FAILED);
        payment.setRetryCount(3);
        payment.setMaxRetries(3);
        return payment;
    }
    
    /**
     * Create an old pending payment
     */
    public static Payment createOldPendingPayment() {
        Payment payment = createPaymentWithStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now().minusHours(2));
        return payment;
    }
    
    /**
     * Create a refund payment
     */
    public static Payment createRefundPayment(Long originalPaymentId) {
        return Payment.builder()
                .customerId(1L)
                .taskerId(2L)
                .taskId(3L)
                .amount(BigDecimal.valueOf(-50.00)) // Negative for refund
                .serviceFee(BigDecimal.ZERO)
                .netAmount(BigDecimal.valueOf(-50.00))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentType(PaymentType.REFUND)
                .status(PaymentStatus.PENDING)
                .description("Refund for payment " + originalPaymentId)
                .metadata("{\"originalPaymentId\":" + originalPaymentId + "}")
                .retryCount(0)
                .maxRetries(3)
                .build();
    }
}
