package com.mshando.paymentservice.service.impl;

import com.mshando.paymentservice.dto.PaymentCreateDTO;
import com.mshando.paymentservice.dto.PaymentRefundDTO;
import com.mshando.paymentservice.dto.PaymentResponseDTO;
import com.mshando.paymentservice.exception.PaymentNotFoundException;
import com.mshando.paymentservice.exception.InvalidPaymentOperationException;
import com.mshando.paymentservice.model.Payment;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;
import com.mshando.paymentservice.repository.PaymentRepository;
import com.mshando.paymentservice.service.PaymentService;
import com.mshando.paymentservice.service.PaymentProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentService for comprehensive payment management.
 * 
 * This service handles all payment-related operations including
 * creation, processing, status management, refunds, and reporting.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentProviderService paymentProviderService;
    @Value("${payment.service-fee-percentage:10.0}")
    private BigDecimal serviceFeePercentage;
    
    @Value("${payment.pending-timeout-minutes:30}")
    private Integer pendingTimeoutMinutes;
    
    @Override
    public PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO, String authenticatedUserId) {
        log.info("Creating payment for customer {} with amount {}", 
                paymentCreateDTO.getCustomerId(), paymentCreateDTO.getAmount());
        
        // Validate business rules
        validatePaymentCreation(paymentCreateDTO, Long.valueOf(authenticatedUserId));
        
        // Check for duplicate bid payments
        if (paymentCreateDTO.getBidId() != null) {
            List<PaymentStatus> activeStatuses = Arrays.asList(
                    PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.COMPLETED);
            
            if (paymentRepository.existsByBidIdAndStatusIn(paymentCreateDTO.getBidId(), activeStatuses)) {
                throw new InvalidPaymentOperationException("Payment already exists for this bid");
            }
        }
        
        // Create payment entity
        Payment payment = buildPaymentFromDTO(paymentCreateDTO);
        payment.calculateServiceFee(serviceFeePercentage);
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        // Process payment asynchronously
        processPaymentAsync(savedPayment.getId());
        
        log.info("Payment created successfully with ID: {}", savedPayment.getId());
        return mapToResponseDTO(savedPayment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long paymentId) {
        log.debug("Fetching payment with ID: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found"));
        
        return mapToResponseDTO(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByExternalTransactionId(String externalTransactionId) {
        log.debug("Fetching payment with external transaction ID: {}", externalTransactionId);
        
        Payment payment = paymentRepository.findByExternalTransactionId(externalTransactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with external transaction ID " + externalTransactionId + " not found"));
        
        return mapToResponseDTO(payment);
    }
    
    @Override
    public PaymentResponseDTO processPayment(Long paymentId) {
        log.info("Processing payment with ID: {}", paymentId);
        
        Payment payment = getPaymentEntity(paymentId);
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentOperationException("Only pending payments can be processed");
        }
        
        try {
            // Update status to processing
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setProcessedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);
            
            // Process with payment provider
            String externalTransactionId = paymentProviderService.processPayment(payment);
            payment.setExternalTransactionId(externalTransactionId);
            
            // Complete payment
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);
            
            log.info("Payment {} processed successfully", paymentId);
            
        } catch (Exception e) {
            log.error("Payment processing failed for payment {}: {}", paymentId, e.getMessage());
            return failPayment(paymentId, e.getMessage());
        }
        
        return mapToResponseDTO(payment);
    }
    
    @Override
    public PaymentResponseDTO completePayment(Long paymentId) {
        log.info("Completing payment with ID: {}", paymentId);
        
        Payment payment = getPaymentEntity(paymentId);
        
        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentOperationException("Only processing payments can be completed");
        }
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        log.info("Payment {} completed successfully", paymentId);
        return mapToResponseDTO(payment);
    }
    
    @Override
    public PaymentResponseDTO failPayment(Long paymentId, String failureReason) {
        log.warn("Failing payment {} with reason: {}", paymentId, failureReason);
        
        Payment payment = getPaymentEntity(paymentId);
        
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        payment.setFailedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        log.info("Payment {} marked as failed", paymentId);
        return mapToResponseDTO(payment);
    }
    
    @Override
    public PaymentResponseDTO retryPayment(Long paymentId) {
        log.info("Retrying payment with ID: {}", paymentId);
        
        Payment payment = getPaymentEntity(paymentId);
        
        if (!payment.canRetry()) {
            throw new InvalidPaymentOperationException("Payment cannot be retried - max retries exceeded or invalid status");
        }
        
        payment.incrementRetryCount();
        payment.setStatus(PaymentStatus.RETRY_PENDING);
        payment = paymentRepository.save(payment);
        
        // Process payment asynchronously
        processPaymentAsync(paymentId);
        
        log.info("Payment {} retry initiated", paymentId);
        return mapToResponseDTO(payment);
    }
    
    @Override
    public PaymentResponseDTO cancelPayment(Long paymentId, String reason) {
        log.info("Cancelling payment {} with reason: {}", paymentId, reason);
        
        Payment payment = getPaymentEntity(paymentId);
        
        if (payment.isFinalState()) {
            throw new InvalidPaymentOperationException("Cannot cancel payment in final state");
        }
        
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason(reason);
        payment = paymentRepository.save(payment);
        
        log.info("Payment {} cancelled successfully", paymentId);
        return mapToResponseDTO(payment);
    }
    
    @Override
    public PaymentResponseDTO refundPayment(Long paymentId, PaymentRefundDTO refundDTO) {
        log.info("Processing refund for payment {} with reason: {}", paymentId, refundDTO.getReason());
        
        Payment originalPayment = getPaymentEntity(paymentId);
        
        if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentOperationException("Only completed payments can be refunded");
        }
        
        BigDecimal refundAmount = refundDTO.getRefundAmount() != null ? 
                refundDTO.getRefundAmount() : originalPayment.getAmount();
        
        if (refundAmount.compareTo(originalPayment.getAmount()) > 0) {
            throw new InvalidPaymentOperationException("Refund amount cannot exceed original payment amount");
        }
        
        // Create refund payment
        Payment refundPayment = Payment.builder()
                .customerId(originalPayment.getCustomerId())
                .taskerId(originalPayment.getTaskerId())
                .taskId(originalPayment.getTaskId())
                .bidId(originalPayment.getBidId())
                .amount(refundAmount.negate()) // Negative amount for refund
                .serviceFee(refundDTO.getRefundServiceFee() ? originalPayment.getServiceFee().negate() : BigDecimal.ZERO)
                .currency(originalPayment.getCurrency())
                .paymentMethod(originalPayment.getPaymentMethod())
                .paymentType(PaymentType.REFUND)
                .status(PaymentStatus.REFUND_PENDING)
                .description("Refund for payment " + paymentId + ": " + refundDTO.getReason())
                .metadata(refundDTO.getMetadata())
                .build();
        
        refundPayment.setNetAmount(refundPayment.getAmount().subtract(refundPayment.getServiceFee()));
        refundPayment = paymentRepository.save(refundPayment);
        
        // Process refund with payment provider
        try {
            paymentProviderService.processRefund(originalPayment, refundPayment);
            
            refundPayment.setStatus(PaymentStatus.REFUNDED);
            refundPayment.setRefundedAt(LocalDateTime.now());
            originalPayment.setRefundedAt(LocalDateTime.now());
            
            paymentRepository.save(refundPayment);
            paymentRepository.save(originalPayment);
            
        } catch (Exception e) {
            log.error("Refund processing failed for payment {}: {}", paymentId, e.getMessage());
            refundPayment.setStatus(PaymentStatus.REFUND_FAILED);
            refundPayment.setFailureReason(e.getMessage());
            paymentRepository.save(refundPayment);
        }
        
        log.info("Refund processed for payment {}", paymentId);
        return mapToResponseDTO(refundPayment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getCustomerPayments(Long customerId, Pageable pageable) {
        log.debug("Fetching payments for customer: {}", customerId);
        
        Page<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return payments.map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getTaskerPayments(Long taskerId, Pageable pageable) {
        log.debug("Fetching payments for tasker: {}", taskerId);
        
        Page<Payment> payments = paymentRepository.findByTaskerIdOrderByCreatedAtDesc(taskerId, pageable);
        return payments.map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getTaskPayments(Long taskId) {
        log.debug("Fetching payments for task: {}", taskId);
        
        List<Payment> payments = paymentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        return payments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        log.debug("Fetching payments with status: {}", status);
        
        Page<Payment> payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return payments.map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByStatusAndType(PaymentStatus status, PaymentType paymentType, Pageable pageable) {
        log.debug("Fetching payments with status: {} and type: {}", status, paymentType);
        
        Page<Payment> payments = paymentRepository.findByStatusAndPaymentTypeOrderByCreatedAtDesc(status, paymentType, pageable);
        return payments.map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsInDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching payments between {} and {}", startDate, endDate);
        
        Page<Payment> payments = paymentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        return payments.map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCustomerTotalPayments(Long customerId) {
        return paymentRepository.calculateTotalPaymentsByCustomer(customerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTaskerTotalEarnings(Long taskerId) {
        return paymentRepository.calculateTotalEarningsByTasker(taskerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateServiceFeesInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.calculateServiceFeesInPeriod(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasCustomerPendingPayments(Long customerId) {
        return paymentRepository.existsByCustomerIdAndStatus(customerId, PaymentStatus.PENDING);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasBidPayments(Long bidId) {
        List<PaymentStatus> activeStatuses = Arrays.asList(
                PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.COMPLETED);
        return paymentRepository.existsByBidIdAndStatusIn(bidId, activeStatuses);
    }
    
    @Override
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void processPendingPayments() {
        log.debug("Processing pending payments...");
        
        Page<Payment> pendingPayments = paymentRepository.findByStatusOrderByCreatedAtDesc(
                PaymentStatus.PENDING, Pageable.ofSize(50));
        
        pendingPayments.forEach(payment -> {
            try {
                processPayment(payment.getId());
            } catch (Exception e) {
                log.error("Failed to process pending payment {}: {}", payment.getId(), e.getMessage());
            }
        });
    }
    
    @Override
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void retryFailedPayments() {
        log.debug("Retrying failed payments...");
        
        List<Payment> retriablePayments = paymentRepository.findRetriablePayments(PaymentStatus.FAILED);
        
        retriablePayments.forEach(payment -> {
            try {
                retryPayment(payment.getId());
            } catch (Exception e) {
                log.error("Failed to retry payment {}: {}", payment.getId(), e.getMessage());
            }
        });
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupOldPendingPayments() {
        log.debug("Cleaning up old pending payments...");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(pendingTimeoutMinutes);
        List<Payment> oldPendingPayments = paymentRepository.findPendingPaymentsOlderThan(cutoffTime);
        
        oldPendingPayments.forEach(payment -> {
            try {
                cancelPayment(payment.getId(), "Payment timeout - automatically cancelled");
            } catch (Exception e) {
                log.error("Failed to cancel old pending payment {}: {}", payment.getId(), e.getMessage());
            }
        });
    }
    
    // Private helper methods
    
    @Async
    private void processPaymentAsync(Long paymentId) {
        try {
            Thread.sleep(1000); // Simulate processing delay
            processPayment(paymentId);
        } catch (Exception e) {
            log.error("Async payment processing failed for payment {}: {}", paymentId, e.getMessage());
        }
    }
    
    private Payment getPaymentEntity(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found"));
    }
    
    private void validatePaymentCreation(PaymentCreateDTO dto, Long authenticatedUserId) {
        // Validate customer authorization
        if (!dto.getCustomerId().equals(authenticatedUserId)) {
            throw new InvalidPaymentOperationException("You can only create payments for yourself");
        }
        
        // Validate amount
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentOperationException("Payment amount must be greater than zero");
        }
        
        // Additional business validations can be added here
    }
    
    private Payment buildPaymentFromDTO(PaymentCreateDTO dto) {
        return Payment.builder()
                .customerId(dto.getCustomerId())
                .taskerId(dto.getTaskerId())
                .taskId(dto.getTaskId())
                .bidId(dto.getBidId())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .paymentMethod(dto.getPaymentMethod())
                .paymentType(dto.getPaymentType())
                .description(dto.getDescription())
                .metadata(dto.getMetadata())
                .paymentIntentId(dto.getPaymentIntentId())
                .status(PaymentStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }
    
    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        PaymentResponseDTO dto = PaymentResponseDTO.builder()
                .id(payment.getId())
                .customerId(payment.getCustomerId())
                .taskerId(payment.getTaskerId())
                .taskId(payment.getTaskId())
                .bidId(payment.getBidId())
                .amount(payment.getAmount())
                .serviceFee(payment.getServiceFee())
                .netAmount(payment.getNetAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .externalTransactionId(payment.getExternalTransactionId())
                .paymentIntentId(payment.getPaymentIntentId())
                .description(payment.getDescription())
                .metadata(payment.getMetadata())
                .failureReason(payment.getFailureReason())
                .retryCount(payment.getRetryCount())
                .maxRetries(payment.getMaxRetries())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .processedAt(payment.getProcessedAt())
                .completedAt(payment.getCompletedAt())
                .failedAt(payment.getFailedAt())
                .refundedAt(payment.getRefundedAt())
                .version(payment.getVersion())
                .build();
        
        // Add related information if needed
        if (payment.getCustomerId() != null) {
            // dto.setCustomerInfo(externalService.getCustomerInfo(payment.getCustomerId()));
        }
        
        if (payment.getTaskerId() != null) {
            // dto.setTaskerInfo(externalService.getTaskerInfo(payment.getTaskerId()));
        }
        
        if (payment.getTaskId() != null) {
            // dto.setTaskInfo(externalService.getTaskInfo(payment.getTaskId()));
        }
        
        return dto;
    }
}
