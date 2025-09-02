package com.mshando.taskservice.repository;

import com.mshando.taskservice.model.Task;
import com.mshando.taskservice.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Task entity
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find tasks by customer ID
     * @param customerId customer ID
     * @param pageable pagination information
     * @return page of tasks
     */
    Page<Task> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Find tasks by assigned tasker ID
     * @param taskerId tasker ID
     * @param pageable pagination information
     * @return page of tasks
     */
    Page<Task> findByAssignedTaskerId(Long taskerId, Pageable pageable);
    
    /**
     * Find tasks by status
     * @param status task status
     * @param pageable pagination information
     * @return page of tasks
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    /**
     * Find tasks by category ID
     * @param categoryId category ID
     * @param pageable pagination information
     * @return page of tasks
     */
    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * Search tasks by title or description
     * @param query search query
     * @param pageable pagination information
     * @return page of tasks
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND t.status = 'PUBLISHED'")
    Page<Task> searchTasks(@Param("query") String query, Pageable pageable);
    
    /**
     * Find tasks by budget range
     * @param minBudget minimum budget
     * @param maxBudget maximum budget
     * @param pageable pagination information
     * @return page of tasks
     */
    Page<Task> findByBudgetBetween(BigDecimal minBudget, BigDecimal maxBudget, Pageable pageable);
    
    /**
     * Find tasks by location
     * @param location location
     * @param pageable pagination information
     * @return page of tasks
     */
    @Query("SELECT t FROM Task t WHERE t.location LIKE CONCAT('%', :location, '%') AND t.status = 'PUBLISHED'")
    Page<Task> findByLocationContaining(@Param("location") String location, Pageable pageable);
    
    /**
     * Find tasks due before a specific date
     * @param dueDate due date
     * @return list of tasks
     */
    List<Task> findByDueDateBeforeAndStatusIn(LocalDateTime dueDate, List<TaskStatus> statuses);
    
    /**
     * Find published tasks with filters
     * @param categoryId category ID (optional)
     * @param minBudget minimum budget (optional)
     * @param maxBudget maximum budget (optional)
     * @param location location (optional)
     * @param isRemote remote work flag (optional)
     * @param pageable pagination information
     * @return page of tasks
     */
    @Query("SELECT t FROM Task t WHERE " +
           "t.status = 'PUBLISHED' " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:minBudget IS NULL OR t.budget >= :minBudget) " +
           "AND (:maxBudget IS NULL OR t.budget <= :maxBudget) " +
           "AND (:location IS NULL OR t.location LIKE CONCAT('%', :location, '%')) " +
           "AND (:isRemote IS NULL OR t.isRemote = :isRemote)")
    Page<Task> findPublishedTasksWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget,
            @Param("location") String location,
            @Param("isRemote") Boolean isRemote,
            Pageable pageable
    );
}
