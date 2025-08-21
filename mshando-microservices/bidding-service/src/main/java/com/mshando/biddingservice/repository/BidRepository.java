package com.mshando.biddingservice.repository;

import com.mshando.biddingservice.model.Bid;
import com.mshando.biddingservice.model.BidStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Bid entity operations.
 * 
 * Provides data access methods for bid management including
 * CRUD operations and custom queries for business logic.
 *
 * @author Mshando Team
 * @version 1.0.0
 */
@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    /**
     * Find all bids for a specific task
     */
    List<Bid> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    /**
     * Find all bids for a specific task with pagination
     */
    Page<Bid> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);
    
    /**
     * Find all bids by a specific tasker
     */
    Page<Bid> findByTaskerIdOrderByCreatedAtDesc(Long taskerId, Pageable pageable);
    
    /**
     * Find all bids for tasks owned by a specific customer
     */
    Page<Bid> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    
    /**
     * Find all bids by status
     */
    Page<Bid> findByStatusOrderByCreatedAtDesc(BidStatus status, Pageable pageable);
    
    /**
     * Find bids by tasker and status
     */
    Page<Bid> findByTaskerIdAndStatusOrderByCreatedAtDesc(Long taskerId, BidStatus status, Pageable pageable);
    
    /**
     * Find bids by customer and status
     */
    Page<Bid> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, BidStatus status, Pageable pageable);
    
    /**
     * Find bids by task and status
     */
    List<Bid> findByTaskIdAndStatus(Long taskId, BidStatus status);
    
    /**
     * Find a specific bid by task and tasker (for uniqueness check)
     */
    Optional<Bid> findByTaskIdAndTaskerId(Long taskId, Long taskerId);
    
    /**
     * Check if a tasker has already bid on a task
     */
    boolean existsByTaskIdAndTaskerId(Long taskId, Long taskerId);
    
    /**
     * Count bids for a specific task
     */
    long countByTaskId(Long taskId);
    
    /**
     * Count bids for a specific task with status
     */
    long countByTaskIdAndStatus(Long taskId, BidStatus status);
    
    /**
     * Count pending bids for a task
     */
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.taskId = :taskId AND b.status = 'PENDING'")
    long countPendingBidsByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find the accepted bid for a task (should be only one)
     */
    Optional<Bid> findByTaskIdAndStatus(Long taskId, BidStatus status);
    
    /**
     * Find pending bids older than specified date (for auto-acceptance)
     */
    @Query("SELECT b FROM Bid b WHERE b.status = 'PENDING' AND b.createdAt < :cutoffDate")
    List<Bid> findPendingBidsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find the lowest bid amount for a task
     */
    @Query("SELECT MIN(b.amount) FROM Bid b WHERE b.taskId = :taskId AND b.status = 'PENDING'")
    Optional<Double> findLowestBidAmountByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find the highest bid amount for a task
     */
    @Query("SELECT MAX(b.amount) FROM Bid b WHERE b.taskId = :taskId AND b.status = 'PENDING'")
    Optional<Double> findHighestBidAmountByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find average bid amount for a task
     */
    @Query("SELECT AVG(b.amount) FROM Bid b WHERE b.taskId = :taskId AND b.status = 'PENDING'")
    Optional<Double> findAverageBidAmountByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Find bids with status change in date range
     */
    @Query("SELECT b FROM Bid b WHERE b.status = :status AND " +
           "((b.acceptedAt BETWEEN :startDate AND :endDate) OR " +
           "(b.rejectedAt BETWEEN :startDate AND :endDate) OR " +
           "(b.withdrawnAt BETWEEN :startDate AND :endDate) OR " +
           "(b.completedAt BETWEEN :startDate AND :endDate) OR " +
           "(b.cancelledAt BETWEEN :startDate AND :endDate))")
    List<Bid> findBidsWithStatusChangeBetween(@Param("status") BidStatus status,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find tasker's bid statistics
     */
    @Query("SELECT NEW map(" +
           "COUNT(b) as totalBids, " +
           "SUM(CASE WHEN b.status = 'ACCEPTED' THEN 1 ELSE 0 END) as acceptedBids, " +
           "SUM(CASE WHEN b.status = 'REJECTED' THEN 1 ELSE 0 END) as rejectedBids, " +
           "SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedBids, " +
           "AVG(b.amount) as averageBidAmount) " +
           "FROM Bid b WHERE b.taskerId = :taskerId")
    Object getTaskerBidStatistics(@Param("taskerId") Long taskerId);
    
    /**
     * Delete bids by task ID (for cleanup when task is deleted)
     */
    void deleteByTaskId(Long taskId);
}
