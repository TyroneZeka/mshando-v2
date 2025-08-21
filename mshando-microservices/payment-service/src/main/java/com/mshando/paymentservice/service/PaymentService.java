package com.mshando.paymentservice.service;

import com.mshando.paymentservice.dto.PaymentCreateDTO;
import com.mshando.paymentservice.dto.PaymentRefundDTO;
import com.mshando.paymentservice.dto.PaymentResponseDTO;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for payment management operations.
 * 
 * Defines the contract for payment processing, including
 * creation, status management, refunds, and reporting.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
public interface PaymentService {
    
    /**
     * Create a new payment
     */
    PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO, String authenticatedUserId);
    
    /**
     * Get payment by ID
     */
    PaymentResponseDTO getPaymentById(Long paymentId);
    
    /**
     * Get payment by external transaction ID
     */
    PaymentResponseDTO getPaymentByExternalTransactionId(String externalTransactionId);
    
    /**
     * Process a pending payment
     */
    PaymentResponseDTO processPayment(Long paymentId);
    
    /**
     * Complete a processed payment
     */
    PaymentResponseDTO completePayment(Long paymentId);
    
    /**
     * Fail a payment with reason
     */
    PaymentResponseDTO failPayment(Long paymentId, String failureReason);
    
    /**
     * Retry a failed payment
     */
    PaymentResponseDTO retryPayment(Long paymentId);
    
    /**
     * Cancel a pending payment
     */
    PaymentResponseDTO cancelPayment(Long paymentId, String reason);
    
    /**
     * Refund a completed payment
     */
    PaymentResponseDTO refundPayment(Long paymentId, PaymentRefundDTO refundDTO);
    
    /**
     * Get payments for a customer
     */
    Page<PaymentResponseDTO> getCustomerPayments(Long customerId, Pageable pageable);
    
    /**
     * Get payments for a tasker
     */
    Page<PaymentResponseDTO> getTaskerPayments(Long taskerId, Pageable pageable);
    
    /**
     * Get payments for a task
     */
    List<PaymentResponseDTO> getTaskPayments(Long taskId);
    
    /**
     * Get payments by status
     */
    Page<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable);
    
    /**
     * Get payments by status and type
     */
    Page<PaymentResponseDTO> getPaymentsByStatusAndType(
            PaymentStatus status, PaymentType paymentType, Pageable pageable);
    
    /**
     * Get payments in date range
     */
    Page<PaymentResponseDTO> getPaymentsInDateRange(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Calculate total payments for customer
     */
    BigDecimal calculateCustomerTotalPayments(Long customerId);
    
    /**
     * Calculate total earnings for tasker
     */
    BigDecimal calculateTaskerTotalEarnings(Long taskerId);
    
    /**
     * Calculate service fees in period
     */
    BigDecimal calculateServiceFeesInPeriod(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Check if customer has pending payments
     */
    boolean hasCustomerPendingPayments(Long customerId);
    
    /**
     * Check if bid already has payments
     */
    boolean hasBidPayments(Long bidId);
    
    /**
     * Process pending payments (scheduled task)
     */
    void processPendingPayments();
    
    /**
     * Retry failed payments (scheduled task)
     */
    void retryFailedPayments();
    
    /**
     * Clean up old pending payments (scheduled task)
     */
    void cleanupOldPendingPayments();
}
