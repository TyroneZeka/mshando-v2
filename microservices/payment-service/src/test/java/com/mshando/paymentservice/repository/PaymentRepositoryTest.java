package com.mshando.paymentservice.repository;

import com.mshando.paymentservice.BaseIntegrationTest;
import com.mshando.paymentservice.TestDataFactory;
import com.mshando.paymentservice.model.Payment;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for PaymentRepository.
 * 
 * Tests all repository methods including custom queries,
 * business logic validations, and data integrity.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@DisplayName("Payment Repository Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentRepositoryTest extends BaseIntegrationTest {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private Payment testPayment;
    
    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        
        testPayment = TestDataFactory.createPayment();
    }
    
    @Test
    @DisplayName("Should save and retrieve payment successfully")
    void shouldSaveAndRetrievePayment() {
        // Given
        Payment payment = TestDataFactory.createPayment();
        
        // When
        Payment savedPayment = paymentRepository.save(payment);
        
        // Then
        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getCreatedAt()).isNotNull();
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
        assertThat(savedPayment.getVersion()).isEqualTo(0L);
        
        Optional<Payment> retrievedPayment = paymentRepository.findById(savedPayment.getId());
        assertThat(retrievedPayment).isPresent();
        assertThat(retrievedPayment.get().getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(retrievedPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
    
    @Test
    @DisplayName("Should find payment by external transaction ID")
    void shouldFindByExternalTransactionId() {
        // Given
        testPayment.setExternalTransactionId("ext_12345");
        paymentRepository.save(testPayment);
        
        // When
        Optional<Payment> found = paymentRepository.findByExternalTransactionId("ext_12345");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getExternalTransactionId()).isEqualTo("ext_12345");
    }
    
    @Test
    @DisplayName("Should find payment by payment intent ID")
    void shouldFindByPaymentIntentId() {
        // Given
        testPayment.setPaymentIntentId("pi_12345");
        paymentRepository.save(testPayment);
        
        // When
        Optional<Payment> found = paymentRepository.findByPaymentIntentId("pi_12345");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentIntentId()).isEqualTo("pi_12345");
    }
    
    @Test
    @DisplayName("Should find payments by customer ID with pagination")
    void shouldFindByCustomerIdWithPagination() {
        // Given
        Long customerId = 100L;
        Payment payment1 = TestDataFactory.createPaymentForCustomer(customerId);
        Payment payment2 = TestDataFactory.createPaymentForCustomer(customerId);
        Payment payment3 = TestDataFactory.createPaymentForCustomer(999L); // Different customer
        
        paymentRepository.saveAll(Arrays.asList(payment1, payment2, payment3));
        
        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> result = paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Payment::getCustomerId)
                .containsOnly(customerId);
    }
    
    @Test
    @DisplayName("Should find payments by tasker ID with pagination")
    void shouldFindByTaskerIdWithPagination() {
        // Given
        Long taskerId = 200L;
        Payment payment1 = TestDataFactory.createPaymentForTasker(taskerId);
        Payment payment2 = TestDataFactory.createPaymentForTasker(taskerId);
        Payment payment3 = TestDataFactory.createPaymentForTasker(999L); // Different tasker
        
        paymentRepository.saveAll(Arrays.asList(payment1, payment2, payment3));
        
        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> result = paymentRepository.findByTaskerIdOrderByCreatedAtDesc(taskerId, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Payment::getTaskerId)
                .containsOnly(taskerId);
    }
    
    @Test
    @DisplayName("Should find payments by task ID")
    void shouldFindByTaskId() {
        // Given
        Long taskId = 300L;
        Payment payment1 = TestDataFactory.createPaymentForTask(taskId);
        Payment payment2 = TestDataFactory.createPaymentForTask(taskId);
        Payment payment3 = TestDataFactory.createPaymentForTask(999L); // Different task
        
        paymentRepository.saveAll(Arrays.asList(payment1, payment2, payment3));
        
        // When
        List<Payment> result = paymentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Payment::getTaskId)
                .containsOnly(taskId);
    }
    
    @Test
    @DisplayName("Should find payments by status")
    void shouldFindByStatus() {
        // Given
        Payment pendingPayment = TestDataFactory.createPaymentWithStatus(PaymentStatus.PENDING);
        Payment completedPayment = TestDataFactory.createPaymentWithStatus(PaymentStatus.COMPLETED);
        Payment failedPayment = TestDataFactory.createPaymentWithStatus(PaymentStatus.FAILED);
        
        paymentRepository.saveAll(Arrays.asList(pendingPayment, completedPayment, failedPayment));
        
        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> completedPayments = paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.COMPLETED, pageable);
        
        // Then
        assertThat(completedPayments.getContent()).hasSize(1);
        assertThat(completedPayments.getContent().get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }
    
    @Test
    @DisplayName("Should find payments by status and payment type")
    void shouldFindByStatusAndPaymentType() {
        // Given
        Payment taskPayment = TestDataFactory.createPaymentWithType(PaymentType.TASK_PAYMENT);
        taskPayment.setStatus(PaymentStatus.COMPLETED);
        
        Payment refundPayment = TestDataFactory.createPaymentWithType(PaymentType.REFUND);
        refundPayment.setStatus(PaymentStatus.COMPLETED);
        
        Payment pendingTaskPayment = TestDataFactory.createPaymentWithType(PaymentType.TASK_PAYMENT);
        pendingTaskPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.saveAll(Arrays.asList(taskPayment, refundPayment, pendingTaskPayment));
        
        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> result = paymentRepository.findByStatusAndPaymentTypeOrderByCreatedAtDesc(
                PaymentStatus.COMPLETED, PaymentType.TASK_PAYMENT, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getContent().get(0).getPaymentType()).isEqualTo(PaymentType.TASK_PAYMENT);
    }
    
    @Test
    @DisplayName("Should find retriable payments")
    void shouldFindRetriablePayments() {
        // Given
        Payment retriablePayment = TestDataFactory.createRetriablePayment();
        Payment maxRetriedPayment = TestDataFactory.createMaxRetriedPayment();
        Payment completedPayment = TestDataFactory.createPaymentWithStatus(PaymentStatus.COMPLETED);
        
        paymentRepository.saveAll(Arrays.asList(retriablePayment, maxRetriedPayment, completedPayment));
        
        // When
        List<Payment> result = paymentRepository.findRetriablePayments(PaymentStatus.FAILED);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetryCount()).isLessThan(result.get(0).getMaxRetries());
    }
    
    @Test
    @DisplayName("Should find pending payments older than specified time")
    void shouldFindPendingPaymentsOlderThan() {
        // Given
        Payment oldPendingPayment = TestDataFactory.createOldPendingPayment();
        Payment recentPendingPayment = TestDataFactory.createPaymentWithStatus(PaymentStatus.PENDING);
        
        paymentRepository.saveAll(Arrays.asList(oldPendingPayment, recentPendingPayment));
        
        // When
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
        List<Payment> result = paymentRepository.findPendingPaymentsOlderThan(cutoffTime);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCreatedAt()).isBefore(cutoffTime);
    }
    
    @Test
    @DisplayName("Should calculate total payments by customer")
    void shouldCalculateTotalPaymentsByCustomer() {
        // Given
        Long customerId = 100L;
        Payment payment1 = TestDataFactory.createPaymentForCustomer(customerId);
        payment1.setAmount(BigDecimal.valueOf(100));
        payment1.setStatus(PaymentStatus.COMPLETED);
        
        Payment payment2 = TestDataFactory.createPaymentForCustomer(customerId);
        payment2.setAmount(BigDecimal.valueOf(200));
        payment2.setStatus(PaymentStatus.COMPLETED);
        
        Payment pendingPayment = TestDataFactory.createPaymentForCustomer(customerId);
        pendingPayment.setAmount(BigDecimal.valueOf(300));
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.saveAll(Arrays.asList(payment1, payment2, pendingPayment));
        
        // When
        BigDecimal total = paymentRepository.calculateTotalPaymentsByCustomer(customerId);
        
        // Then
        assertThat(total).isEqualTo(BigDecimal.valueOf(300)); // Only completed payments
    }
    
    @Test
    @DisplayName("Should calculate total earnings by tasker")
    void shouldCalculateTotalEarningsByTasker() {
        // Given
        Long taskerId = 200L;
        Payment payment1 = TestDataFactory.createPaymentForTasker(taskerId);
        payment1.setNetAmount(BigDecimal.valueOf(90));
        payment1.setStatus(PaymentStatus.COMPLETED);
        
        Payment payment2 = TestDataFactory.createPaymentForTasker(taskerId);
        payment2.setNetAmount(BigDecimal.valueOf(180));
        payment2.setStatus(PaymentStatus.COMPLETED);
        
        Payment pendingPayment = TestDataFactory.createPaymentForTasker(taskerId);
        pendingPayment.setNetAmount(BigDecimal.valueOf(270));
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.saveAll(Arrays.asList(payment1, payment2, pendingPayment));
        
        // When
        BigDecimal total = paymentRepository.calculateTotalEarningsByTasker(taskerId);
        
        // Then
        assertThat(total).isEqualTo(BigDecimal.valueOf(270)); // Only completed payments
    }
    
    @Test
    @DisplayName("Should calculate service fees in period")
    void shouldCalculateServiceFeesInPeriod() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        Payment payment1 = TestDataFactory.createPaymentWithStatus(PaymentStatus.COMPLETED);
        payment1.setServiceFee(BigDecimal.valueOf(10));
        payment1.setCreatedAt(LocalDateTime.now());
        
        Payment payment2 = TestDataFactory.createPaymentWithStatus(PaymentStatus.COMPLETED);
        payment2.setServiceFee(BigDecimal.valueOf(15));
        payment2.setCreatedAt(LocalDateTime.now());
        
        // Payment outside the period
        Payment oldPayment = TestDataFactory.createPaymentWithStatus(PaymentStatus.COMPLETED);
        oldPayment.setServiceFee(BigDecimal.valueOf(20));
        oldPayment.setCreatedAt(LocalDateTime.now().minusDays(2));
        
        paymentRepository.saveAll(Arrays.asList(payment1, payment2, oldPayment));
        
        // When
        BigDecimal total = paymentRepository.calculateServiceFeesInPeriod(startDate, endDate);
        
        // Then
        assertThat(total).isEqualTo(BigDecimal.valueOf(25)); // Only payments in period
    }
    
    @Test
    @DisplayName("Should count payments by customer and status")
    void shouldCountByCustomerIdAndStatus() {
        // Given
        Long customerId = 100L;
        Payment completedPayment1 = TestDataFactory.createPaymentForCustomer(customerId);
        completedPayment1.setStatus(PaymentStatus.COMPLETED);
        
        Payment completedPayment2 = TestDataFactory.createPaymentForCustomer(customerId);
        completedPayment2.setStatus(PaymentStatus.COMPLETED);
        
        Payment pendingPayment = TestDataFactory.createPaymentForCustomer(customerId);
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.saveAll(Arrays.asList(completedPayment1, completedPayment2, pendingPayment));
        
        // When
        Long completedCount = paymentRepository.countByCustomerIdAndStatus(customerId, PaymentStatus.COMPLETED);
        Long pendingCount = paymentRepository.countByCustomerIdAndStatus(customerId, PaymentStatus.PENDING);
        
        // Then
        assertThat(completedCount).isEqualTo(2L);
        assertThat(pendingCount).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should check if customer has pending payments")
    void shouldCheckCustomerHasPendingPayments() {
        // Given
        Long customerId = 100L;
        Payment pendingPayment = TestDataFactory.createPaymentForCustomer(customerId);
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.save(pendingPayment);
        
        // When
        boolean hasPending = paymentRepository.existsByCustomerIdAndStatus(customerId, PaymentStatus.PENDING);
        boolean hasCompleted = paymentRepository.existsByCustomerIdAndStatus(customerId, PaymentStatus.COMPLETED);
        
        // Then
        assertThat(hasPending).isTrue();
        assertThat(hasCompleted).isFalse();
    }
    
    @Test
    @DisplayName("Should check if bid has existing payments")
    void shouldCheckBidHasExistingPayments() {
        // Given
        Long bidId = 400L;
        Payment payment = TestDataFactory.createPayment();
        payment.setBidId(bidId);
        payment.setStatus(PaymentStatus.COMPLETED);
        
        paymentRepository.save(payment);
        
        // When
        List<PaymentStatus> activeStatuses = Arrays.asList(
                PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.COMPLETED);
        boolean hasPayments = paymentRepository.existsByBidIdAndStatusIn(bidId, activeStatuses);
        
        // Then
        assertThat(hasPayments).isTrue();
    }
    
    @Test
    @DisplayName("Should update payment status")
    void shouldUpdatePaymentStatus() {
        // Given
        Payment payment = paymentRepository.save(testPayment);
        
        // When
        int updatedRows = paymentRepository.updatePaymentStatus(payment.getId(), PaymentStatus.PROCESSING);
        
        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
    }
    
    @Test
    @DisplayName("Should increment retry count")
    void shouldIncrementRetryCount() {
        // Given
        testPayment.setRetryCount(1);
        Payment payment = paymentRepository.save(testPayment);
        
        // When
        int updatedRows = paymentRepository.incrementRetryCount(payment.getId());
        
        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updatedPayment.getRetryCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should find task payments")
    void shouldFindTaskPayments() {
        // Given
        Long taskId = 300L;
        Payment taskPayment = TestDataFactory.createPaymentForTask(taskId);
        taskPayment.setPaymentType(PaymentType.TASK_PAYMENT);
        
        Payment refundPayment = TestDataFactory.createPaymentForTask(taskId);
        refundPayment.setPaymentType(PaymentType.REFUND);
        
        paymentRepository.saveAll(Arrays.asList(taskPayment, refundPayment));
        
        // When
        List<Payment> result = paymentRepository.findTaskPayments(taskId);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentType()).isEqualTo(PaymentType.TASK_PAYMENT);
    }
    
    @Test
    @DisplayName("Should find recent payments")
    void shouldFindRecentPayments() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        
        Payment recentPayment = TestDataFactory.createPayment();
        recentPayment.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        
        Payment oldPayment = TestDataFactory.createPayment();
        oldPayment.setCreatedAt(LocalDateTime.now().minusHours(2));
        
        paymentRepository.saveAll(Arrays.asList(recentPayment, oldPayment));
        
        // When
        List<Payment> result = paymentRepository.findRecentPayments(since);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCreatedAt()).isAfter(since);
    }
    
    @Test
    @DisplayName("Should handle payment entity business logic")
    void shouldHandlePaymentEntityBusinessLogic() {
        // Given
        Payment payment = TestDataFactory.createPayment();
        
        // Test service fee calculation
        payment.calculateServiceFee(BigDecimal.valueOf(10)); // 10%
        assertThat(payment.getServiceFee()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(payment.getNetAmount()).isEqualTo(BigDecimal.valueOf(90.00));
        
        // Test retry logic
        assertThat(payment.canRetry()).isTrue();
        payment.incrementRetryCount();
        assertThat(payment.getRetryCount()).isEqualTo(1);
        
        // Test final state check
        assertThat(payment.isFinalState()).isFalse();
        payment.setStatus(PaymentStatus.COMPLETED);
        assertThat(payment.isFinalState()).isTrue();
    }
}