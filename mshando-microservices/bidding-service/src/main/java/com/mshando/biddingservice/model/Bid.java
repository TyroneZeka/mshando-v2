package com.mshando.biddingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a bid placed by a tasker for a specific task.
 * 
 * A bid contains the proposed amount, message, and status information
 * for a tasker's offer to complete a specific task.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Entity
@Table(name = "bids")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    @NotNull(message = "Task ID is required")
    private Long taskId;

    @Column(name = "tasker_id", nullable = false)
    @NotNull(message = "Tasker ID is required")
    private Long taskerId;

    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "5.0", message = "Minimum bid amount is $5.00")
    @DecimalMax(value = "10000.0", message = "Maximum bid amount is $10,000.00")
    private BigDecimal amount;

    @Column(name = "message", length = 1000)
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BidStatus status = BidStatus.PENDING;

    @Column(name = "estimated_completion_hours")
    @Min(value = 1, message = "Estimated completion time must be at least 1 hour")
    @Max(value = 720, message = "Estimated completion time cannot exceed 720 hours (30 days)")
    private Integer estimatedCompletionHours;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Additional indexes for performance
    @Table(indexes = {
        @Index(name = "idx_bid_task_id", columnList = "task_id"),
        @Index(name = "idx_bid_tasker_id", columnList = "tasker_id"),
        @Index(name = "idx_bid_customer_id", columnList = "customer_id"),
        @Index(name = "idx_bid_status", columnList = "status"),
        @Index(name = "idx_bid_created_at", columnList = "created_at"),
        @Index(name = "idx_bid_task_status", columnList = "task_id, status")
    })
    
    /**
     * Checks if the bid is in a pending state
     */
    public boolean isPending() {
        return status == BidStatus.PENDING;
    }

    /**
     * Checks if the bid has been accepted
     */
    public boolean isAccepted() {
        return status == BidStatus.ACCEPTED;
    }

    /**
     * Checks if the bid has been rejected
     */
    public boolean isRejected() {
        return status == BidStatus.REJECTED;
    }

    /**
     * Checks if the bid has been withdrawn
     */
    public boolean isWithdrawn() {
        return status == BidStatus.WITHDRAWN;
    }

    /**
     * Checks if the bid has been completed
     */
    public boolean isCompleted() {
        return status == BidStatus.COMPLETED;
    }

    /**
     * Checks if the bid has been cancelled
     */
    public boolean isCancelled() {
        return status == BidStatus.CANCELLED;
    }

    /**
     * Checks if the bid can be modified (only pending bids can be modified)
     */
    public boolean canBeModified() {
        return isPending();
    }

    /**
     * Checks if the bid can be withdrawn
     */
    public boolean canBeWithdrawn() {
        return isPending() || isAccepted();
    }

    /**
     * Checks if the bid is in a final state (cannot be changed)
     */
    public boolean isFinalState() {
        return isRejected() || isWithdrawn() || isCompleted() || isCancelled();
    }
}
