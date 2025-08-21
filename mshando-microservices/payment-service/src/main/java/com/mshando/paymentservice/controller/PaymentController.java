package com.mshando.paymentservice.controller;

import com.mshando.paymentservice.dto.PaymentCreateDTO;
import com.mshando.paymentservice.dto.PaymentRefundDTO;
import com.mshando.paymentservice.dto.PaymentResponseDTO;
import com.mshando.paymentservice.model.PaymentStatus;
import com.mshando.paymentservice.model.PaymentType;
import com.mshando.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for payment management operations.
 * 
 * Provides comprehensive endpoints for payment processing,
 * status management, refunds, and financial reporting.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "💳 Payment Management", 
     description = "Complete payment processing including transactions, refunds, and financial reporting")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @Operation(
        summary = "💰 Create New Payment",
        description = """
                **Create a new payment transaction**
                
                This endpoint allows users to initiate payment transactions for tasks, services, or other platform activities.
                
                ### Business Rules:
                - ✅ Amount must be greater than $0.01
                - 💰 Maximum payment amount: $100,000
                - 🔒 Users can only create payments for themselves
                - 📝 Description is optional but recommended
                
                ### Payment Types:
                - **TASK_PAYMENT**: Payment for completed tasks
                - **SERVICE_FEE**: Platform service charges
                - **DEPOSIT**: Adding funds to platform balance
                - **REFUND**: Refunding previous payments
                
                ### Workflow:
                1. User submits payment request
                2. System validates business rules
                3. Payment is created with PENDING status
                4. Payment is processed asynchronously
                5. Status updates are sent to involved parties
                """,
        tags = {"Payment Creation"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "✅ Payment created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentResponseDTO.class),
                examples = @ExampleObject(
                    name = "Successful Payment Creation",
                    summary = "Example of successful payment creation",
                    value = """
                            {
                              "id": 123,
                              "customerId": 456,
                              "taskerId": 789,
                              "taskId": 101,
                              "amount": 150.00,
                              "serviceFee": 15.00,
                              "netAmount": 135.00,
                              "currency": "USD",
                              "paymentMethod": "CREDIT_CARD",
                              "paymentType": "TASK_PAYMENT",
                              "status": "PENDING",
                              "description": "Payment for task completion",
                              "createdAt": "2025-08-21T10:30:00",
                              "retryCount": 0,
                              "maxRetries": 3
                            }
                            """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "❌ Invalid payment data"),
        @ApiResponse(responseCode = "401", description = "🔒 Authentication required"),
        @ApiResponse(responseCode = "409", description = "⚠️ Business rule violation")
    })
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Payment creation details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = PaymentCreateDTO.class),
                    examples = @ExampleObject(
                        name = "Create Payment Example",
                        summary = "Example payment creation request",
                        value = """
                                {
                                  "customerId": 456,
                                  "taskerId": 789,
                                  "taskId": 101,
                                  "amount": 150.00,
                                  "currency": "USD",
                                  "paymentMethod": "CREDIT_CARD",
                                  "paymentType": "TASK_PAYMENT",
                                  "description": "Payment for task completion"
                                }
                                """
                    )
                )
            )
            @Valid @RequestBody PaymentCreateDTO paymentCreateDTO,
            @Parameter(hidden = true) Authentication authentication) {
        
        log.info("Creating payment for customer {} with amount {}", 
                paymentCreateDTO.getCustomerId(), paymentCreateDTO.getAmount());
        
        PaymentResponseDTO createdPayment = paymentService.createPayment(paymentCreateDTO, authentication.getName());
        
        log.info("Payment created successfully with ID: {}", createdPayment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }
    
    @Operation(
        summary = "🔍 Get Payment Details",
        description = """
                **Retrieve detailed information about a specific payment**
                
                Returns complete payment information including status, timestamps, and transaction details.
                """,
        tags = {"Payment Retrieval"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Payment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "❓ Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "Unique payment identifier", example = "123")
            @PathVariable Long paymentId) {
        
        log.debug("Fetching payment with ID: {}", paymentId);
        
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @Operation(
        summary = "🔎 Get Payment by External Transaction ID",
        description = """
                **Retrieve payment information using external payment provider transaction ID**
                
                Useful for webhook processing and payment reconciliation.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/external/{externalTransactionId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByExternalTransactionId(
            @Parameter(description = "External payment provider transaction ID", example = "stripe_12345")
            @PathVariable String externalTransactionId) {
        
        log.debug("Fetching payment with external transaction ID: {}", externalTransactionId);
        
        PaymentResponseDTO payment = paymentService.getPaymentByExternalTransactionId(externalTransactionId);
        return ResponseEntity.ok(payment);
    }
    
    @Operation(
        summary = "⚡ Process Payment",
        description = """
                **Manually trigger payment processing**
                
                Forces processing of a pending payment. Typically used for administrative purposes.
                """,
        tags = {"Payment Processing"}
    )
    @PatchMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Parameter(description = "Payment ID to process", example = "123")
            @PathVariable Long paymentId) {
        
        log.info("Processing payment with ID: {}", paymentId);
        
        PaymentResponseDTO processedPayment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(processedPayment);
    }
    
    @Operation(
        summary = "✅ Complete Payment",
        description = """
                **Mark a processing payment as completed**
                
                Used when payment has been successfully processed by external provider.
                """,
        tags = {"Payment Processing"}
    )
    @PatchMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponseDTO> completePayment(
            @Parameter(description = "Payment ID to complete", example = "123")
            @PathVariable Long paymentId) {
        
        log.info("Completing payment with ID: {}", paymentId);
        
        PaymentResponseDTO completedPayment = paymentService.completePayment(paymentId);
        return ResponseEntity.ok(completedPayment);
    }
    
    @Operation(
        summary = "🔄 Retry Failed Payment",
        description = """
                **Retry a failed payment**
                
                Attempts to process a failed payment again if retry limit hasn't been exceeded.
                """,
        tags = {"Payment Processing"}
    )
    @PatchMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponseDTO> retryPayment(
            @Parameter(description = "Payment ID to retry", example = "123")
            @PathVariable Long paymentId) {
        
        log.info("Retrying payment with ID: {}", paymentId);
        
        PaymentResponseDTO retriedPayment = paymentService.retryPayment(paymentId);
        return ResponseEntity.ok(retriedPayment);
    }
    
    @Operation(
        summary = "❌ Cancel Payment",
        description = """
                **Cancel a pending payment**
                
                Cancels a payment that hasn't been processed yet.
                """,
        tags = {"Payment Processing"}
    )
    @PatchMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(
            @Parameter(description = "Payment ID to cancel", example = "123")
            @PathVariable Long paymentId,
            @Parameter(description = "Cancellation reason", example = "Customer request")
            @RequestParam String reason) {
        
        log.info("Cancelling payment {} with reason: {}", paymentId, reason);
        
        PaymentResponseDTO cancelledPayment = paymentService.cancelPayment(paymentId, reason);
        return ResponseEntity.ok(cancelledPayment);
    }
    
    @Operation(
        summary = "💸 Process Refund",
        description = """
                **Process a refund for a completed payment**
                
                Creates and processes a refund transaction for a previously completed payment.
                """,
        tags = {"Payment Processing"}
    )
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(
            @Parameter(description = "Payment ID to refund", example = "123")
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentRefundDTO refundDTO) {
        
        log.info("Processing refund for payment {} with reason: {}", paymentId, refundDTO.getReason());
        
        PaymentResponseDTO refundPayment = paymentService.refundPayment(paymentId, refundDTO);
        return ResponseEntity.ok(refundPayment);
    }
    
    @Operation(
        summary = "📋 Get Customer Payments",
        description = """
                **Retrieve all payments for a specific customer**
                
                Returns paginated list of all payments made by the specified customer.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<PaymentResponseDTO>> getCustomerPayments(
            @Parameter(description = "Customer ID", example = "456")
            @PathVariable Long customerId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of payments per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching payments for customer {} - page: {}, size: {}", customerId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getCustomerPayments(customerId, pageable);
        
        return ResponseEntity.ok(payments);
    }
    
    @Operation(
        summary = "💼 Get Tasker Payments",
        description = """
                **Retrieve all payments for a specific tasker**
                
                Returns paginated list of all payments received by the specified tasker.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/tasker/{taskerId}")
    public ResponseEntity<Page<PaymentResponseDTO>> getTaskerPayments(
            @Parameter(description = "Tasker ID", example = "789")
            @PathVariable Long taskerId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of payments per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching payments for tasker {} - page: {}, size: {}", taskerId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getTaskerPayments(taskerId, pageable);
        
        return ResponseEntity.ok(payments);
    }
    
    @Operation(
        summary = "📄 Get Task Payments",
        description = """
                **Retrieve all payments for a specific task**
                
                Returns all payments related to the specified task.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<PaymentResponseDTO>> getTaskPayments(
            @Parameter(description = "Task ID", example = "101")
            @PathVariable Long taskId) {
        
        log.debug("Fetching payments for task: {}", taskId);
        
        List<PaymentResponseDTO> payments = paymentService.getTaskPayments(taskId);
        return ResponseEntity.ok(payments);
    }
    
    @Operation(
        summary = "📊 Get Payments by Status",
        description = """
                **Retrieve payments filtered by status**
                
                Returns paginated list of payments with the specified status.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PaymentResponseDTO>> getPaymentsByStatus(
            @Parameter(description = "Payment status", example = "COMPLETED")
            @PathVariable PaymentStatus status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of payments per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching payments with status {} - page: {}, size: {}", status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getPaymentsByStatus(status, pageable);
        
        return ResponseEntity.ok(payments);
    }
    
    @Operation(
        summary = "📈 Get Payments by Status and Type",
        description = """
                **Retrieve payments filtered by status and type**
                
                Returns paginated list of payments with the specified status and type.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/filter")
    public ResponseEntity<Page<PaymentResponseDTO>> getPaymentsByStatusAndType(
            @Parameter(description = "Payment status", example = "COMPLETED")
            @RequestParam PaymentStatus status,
            @Parameter(description = "Payment type", example = "TASK_PAYMENT")
            @RequestParam PaymentType paymentType,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of payments per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching payments with status {} and type {} - page: {}, size: {}", 
                status, paymentType, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getPaymentsByStatusAndType(status, paymentType, pageable);
        
        return ResponseEntity.ok(payments);
    }
    
    @Operation(
        summary = "📅 Get Payments in Date Range",
        description = """
                **Retrieve payments within a specific date range**
                
                Returns paginated list of payments created between the specified dates.
                """,
        tags = {"Payment Retrieval"}
    )
    @GetMapping("/date-range")
    public ResponseEntity<Page<PaymentResponseDTO>> getPaymentsInDateRange(
            @Parameter(description = "Start date (ISO format)", example = "2025-08-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", example = "2025-08-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of payments per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching payments between {} and {} - page: {}, size: {}", 
                startDate, endDate, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponseDTO> payments = paymentService.getPaymentsInDateRange(startDate, endDate, pageable);
        
        return ResponseEntity.ok(payments);
    }
    
    @Operation(
        summary = "💰 Get Customer Total Payments",
        description = """
                **Calculate total payment amount for a customer**
                
                Returns the sum of all completed payments made by the customer.
                """,
        tags = {"Financial Analytics"}
    )
    @GetMapping("/customer/{customerId}/total")
    public ResponseEntity<BigDecimal> getCustomerTotalPayments(
            @Parameter(description = "Customer ID", example = "456")
            @PathVariable Long customerId) {
        
        log.debug("Calculating total payments for customer: {}", customerId);
        
        BigDecimal total = paymentService.calculateCustomerTotalPayments(customerId);
        return ResponseEntity.ok(total);
    }
    
    @Operation(
        summary = "💼 Get Tasker Total Earnings",
        description = """
                **Calculate total earnings for a tasker**
                
                Returns the sum of all net amounts received by the tasker.
                """,
        tags = {"Financial Analytics"}
    )
    @GetMapping("/tasker/{taskerId}/earnings")
    public ResponseEntity<BigDecimal> getTaskerTotalEarnings(
            @Parameter(description = "Tasker ID", example = "789")
            @PathVariable Long taskerId) {
        
        log.debug("Calculating total earnings for tasker: {}", taskerId);
        
        BigDecimal total = paymentService.calculateTaskerTotalEarnings(taskerId);
        return ResponseEntity.ok(total);
    }
    
    @Operation(
        summary = "🏦 Get Service Fees in Period",
        description = """
                **Calculate service fees collected in a specific period**
                
                Returns the sum of all service fees collected between the specified dates.
                """,
        tags = {"Financial Analytics"}
    )
    @GetMapping("/service-fees")
    public ResponseEntity<BigDecimal> getServiceFeesInPeriod(
            @Parameter(description = "Start date (ISO format)", example = "2025-08-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", example = "2025-08-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Calculating service fees between {} and {}", startDate, endDate);
        
        BigDecimal total = paymentService.calculateServiceFeesInPeriod(startDate, endDate);
        return ResponseEntity.ok(total);
    }
    
    @Operation(
        summary = "❓ Check Customer Pending Payments",
        description = """
                **Check if customer has any pending payments**
                
                Returns true if the customer has any payments in pending status.
                """,
        tags = {"Payment Validation"}
    )
    @GetMapping("/customer/{customerId}/has-pending")
    public ResponseEntity<Boolean> hasCustomerPendingPayments(
            @Parameter(description = "Customer ID", example = "456")
            @PathVariable Long customerId) {
        
        log.debug("Checking pending payments for customer: {}", customerId);
        
        boolean hasPending = paymentService.hasCustomerPendingPayments(customerId);
        return ResponseEntity.ok(hasPending);
    }
    
    @Operation(
        summary = "🎯 Check Bid Payments",
        description = """
                **Check if bid already has associated payments**
                
                Returns true if the bid has any payments in active status.
                """,
        tags = {"Payment Validation"}
    )
    @GetMapping("/bid/{bidId}/has-payments")
    public ResponseEntity<Boolean> hasBidPayments(
            @Parameter(description = "Bid ID", example = "555")
            @PathVariable Long bidId) {
        
        log.debug("Checking payments for bid: {}", bidId);
        
        boolean hasPayments = paymentService.hasBidPayments(bidId);
        return ResponseEntity.ok(hasPayments);
    }
}
