package com.mshando.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment transaction in the system.
 * 
 * This entity tracks all payment-related information including
 * transaction details, payment methods, status, and audit information.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_customer_id", columnList = "customer_id"),
    @Index(name = "idx_payment_tasker_id", columnList = "tasker_id"),
    @Index(name = "idx_payment_task_id", columnList = "task_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_created_at", columnList = "created_at"),
    @Index(name = "idx_payment_external_transaction_id", columnList = "external_transaction_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID of the customer making the payment
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    /**
     * ID of the tasker receiving the payment (optional for some payment types)
     */
    @Column(name = "tasker_id")
    private Long taskerId;
    
    /**
     * ID of the task this payment is for (optional for some payment types)
     */
    @Column(name = "task_id")
    private Long taskId;
    
    /**
     * ID of the bid this payment is for (optional)
     */
    @Column(name = "bid_id")
    private Long bidId;
    
    /**
     * Payment amount
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * Service fee amount (charged by platform)
     */
    @Column(name = "service_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal serviceFee = BigDecimal.ZERO;
    
    /**
     * Net amount received by tasker (amount - service_fee)
     */
    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;
    
    /**
     * Currency code (ISO 4217)
     */
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";
    
    /**
     * Payment method used
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    /**
     * Type of payment
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;
    
    /**
     * Current payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    /**
     * External transaction ID from payment provider
     */
    @Column(name = "external_transaction_id")
    private String externalTransactionId;
    
    /**
     * Payment intent ID (for Stripe, etc.)
     */
    @Column(name = "payment_intent_id")
    private String paymentIntentId;
    
    /**
     * Description of the payment
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Additional metadata as JSON
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Failure reason if payment failed
     */
    @Column(name = "failure_reason", length = 1000)
    private String failureReason;
    
    /**
     * Number of retry attempts
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    /**
     * Maximum retry attempts allowed
     */
    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    /**
     * Version for optimistic locking
     */
    @Version
    private Long version;
    
    /**
     * Calculate service fee based on amount and platform rates
     */
    public void calculateServiceFee(BigDecimal feePercentage) {
        if (amount != null && feePercentage != null) {
            this.serviceFee = amount.multiply(feePercentage).divide(BigDecimal.valueOf(100));
            this.netAmount = amount.subtract(this.serviceFee);
        }
    }
    
    /**
     * Check if payment can be retried
     */
    public boolean canRetry() {
        return retryCount < maxRetries && 
               (status == PaymentStatus.FAILED || status == PaymentStatus.RETRY_PENDING);
    }
    
    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    /**
     * Check if payment is in final state
     */
    public boolean isFinalState() {
        return status == PaymentStatus.COMPLETED || 
               status == PaymentStatus.REFUNDED || 
               status == PaymentStatus.CANCELLED ||
               (status == PaymentStatus.FAILED && !canRetry());
    }
}
