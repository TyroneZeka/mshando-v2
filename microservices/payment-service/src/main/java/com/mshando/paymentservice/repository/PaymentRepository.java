package com.mshando.paymentservice.repository;

import com.mshando.paymentservice.model.Payment;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 * 
 * Provides comprehensive data access methods for payment management
 * including custom queries for business logic and reporting.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by external transaction ID
     */
    Optional<Payment> findByExternalTransactionId(String externalTransactionId);
    
    /**
     * Find payment by payment intent ID
     */
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    
    /**
     * Find all payments for a specific customer
     */
    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    
    /**
     * Find all payments for a specific tasker
     */
    Page<Payment> findByTaskerIdOrderByCreatedAtDesc(Long taskerId, Pageable pageable);
    
    /**
     * Find all payments for a specific task
     */
    List<Payment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    /**
     * Find all payments for a specific bid
     */
    List<Payment> findByBidIdOrderByCreatedAtDesc(Long bidId);
    
    /**
     * Find payments by status
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);
    
    /**
     * Find payments by status and payment type
     */
    Page<Payment> findByStatusAndPaymentTypeOrderByCreatedAtDesc(
            PaymentStatus status, PaymentType paymentType, Pageable pageable);
    
    /**
     * Find payments by customer and status
     */
    Page<Payment> findByCustomerIdAndStatusOrderByCreatedAtDesc(
            Long customerId, PaymentStatus status, Pageable pageable);
    
    /**
     * Find payments by tasker and status
     */
    Page<Payment> findByTaskerIdAndStatusOrderByCreatedAtDesc(
            Long taskerId, PaymentStatus status, Pageable pageable);
    
    /**
     * Find payments created between dates
     */
    Page<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find payments that can be retried (failed and retry count < max retries)
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.retryCount < p.maxRetries")
    List<Payment> findRetriablePayments(@Param("status") PaymentStatus status);
    
    /**
     * Find pending payments older than specified minutes
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findPendingPaymentsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Calculate total payment amount for customer
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.customerId = :customerId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsByCustomer(@Param("customerId") Long customerId);
    
    /**
     * Calculate total earnings for tasker
     */
    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payment p WHERE p.taskerId = :taskerId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalEarningsByTasker(@Param("taskerId") Long taskerId);
    
    /**
     * Calculate total service fees collected
     */
    @Query("SELECT COALESCE(SUM(p.serviceFee), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateServiceFeesInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count payments by status for customer
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
    Long countByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") PaymentStatus status);
    
    /**
     * Count payments by status for tasker
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.taskerId = :taskerId AND p.status = :status")
    Long countByTaskerIdAndStatus(@Param("taskerId") Long taskerId, @Param("status") PaymentStatus status);
    
    /**
     * Find payments with failed status and max retries exceeded
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount >= p.maxRetries")
    List<Payment> findFailedPaymentsWithMaxRetries();
    
    /**
     * Update payment status
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :paymentId")
    int updatePaymentStatus(@Param("paymentId") Long paymentId, @Param("status") PaymentStatus status);
    
    /**
     * Increment retry count
     */
    @Modifying
    @Query("UPDATE Payment p SET p.retryCount = p.retryCount + 1, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :paymentId")
    int incrementRetryCount(@Param("paymentId") Long paymentId);
    
    /**
     * Find payments for task completion (used for analytics)
     */
    @Query("SELECT p FROM Payment p WHERE p.taskId = :taskId AND p.paymentType = 'TASK_PAYMENT' ORDER BY p.createdAt DESC")
    List<Payment> findTaskPayments(@Param("taskId") Long taskId);
    
    /**
     * Check if customer has any pending payments
     */
    boolean existsByCustomerIdAndStatus(Long customerId, PaymentStatus status);
    
    /**
     * Check if there's already a payment for a specific bid
     */
    boolean existsByBidIdAndStatusIn(Long bidId, List<PaymentStatus> statuses);
    
    /**
     * Find recent payments for audit purposes
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Payment> findRecentPayments(@Param("since") LocalDateTime since);
}
