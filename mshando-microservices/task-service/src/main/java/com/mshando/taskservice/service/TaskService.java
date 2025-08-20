package com.mshando.taskservice.service;

import com.mshando.taskservice.dto.request.TaskCreateRequestDTO;
import com.mshando.taskservice.dto.response.TaskResponseDTO;
import com.mshando.taskservice.exception.CategoryNotFoundException;
import com.mshando.taskservice.exception.TaskNotFoundException;
import com.mshando.taskservice.exception.UnauthorizedAccessException;
import com.mshando.taskservice.model.Category;
import com.mshando.taskservice.model.Task;
import com.mshando.taskservice.model.enums.TaskStatus;
import com.mshando.taskservice.repository.CategoryRepository;
import com.mshando.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing Tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Create a new task
     * @param requestDTO task creation request
     * @param customerId customer ID from JWT token
     * @return created task response
     */
    public TaskResponseDTO createTask(TaskCreateRequestDTO requestDTO, Long customerId) {
        log.info("Creating new task for customer ID: {}", customerId);
        
        // Validate category exists and is active
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + requestDTO.getCategoryId()));
        
        if (!category.getIsActive()) {
            throw new IllegalArgumentException("Cannot create task for inactive category");
        }
        
        Task task = Task.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .category(category)
                .customerId(customerId)
                .budget(requestDTO.getBudget())
                .location(requestDTO.getLocation())
                .isRemote(requestDTO.getIsRemote())
                .dueDate(requestDTO.getDueDate())
                .priority(requestDTO.getPriority())
                .status(TaskStatus.DRAFT)
                .build();
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        
        return mapToResponseDTO(savedTask);
    }
    
    /**
     * Get task by ID
     * @param id task ID
     * @return task response
     */
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
        
        return mapToResponseDTO(task);
    }
    
    /**
     * Get tasks by customer ID
     * @param customerId customer ID
     * @param pageable pagination information
     * @return page of tasks
     */
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching tasks for customer ID: {}", customerId);
        
        Page<Task> taskPage = taskRepository.findByCustomerId(customerId, pageable);
        return taskPage.map(this::mapToResponseDTO);
    }
    
    /**
     * Get tasks by assigned tasker ID
     * @param taskerId tasker ID
     * @param pageable pagination information
     * @return page of tasks
     */
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByTaskerId(Long taskerId, Pageable pageable) {
        log.debug("Fetching tasks for tasker ID: {}", taskerId);
        
        Page<Task> taskPage = taskRepository.findByAssignedTaskerId(taskerId, pageable);
        return taskPage.map(this::mapToResponseDTO);
    }
    
    /**
     * Search published tasks with filters
     * @param categoryId category ID (optional)
     * @param minBudget minimum budget (optional)
     * @param maxBudget maximum budget (optional)
     * @param location location (optional)
     * @param isRemote remote work flag (optional)
     * @param pageable pagination information
     * @return page of published tasks
     */
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> searchPublishedTasks(Long categoryId, BigDecimal minBudget, 
            BigDecimal maxBudget, String location, Boolean isRemote, Pageable pageable) {
        log.debug("Searching published tasks with filters");
        
        Page<Task> taskPage = taskRepository.findPublishedTasksWithFilters(
                categoryId, minBudget, maxBudget, location, isRemote, pageable);
        
        return taskPage.map(this::mapToResponseDTO);
    }
    
    /**
     * Update task
     * @param id task ID
     * @param requestDTO update request
     * @param userId user ID from JWT token
     * @return updated task response
     */
    public TaskResponseDTO updateTask(Long id, TaskCreateRequestDTO requestDTO, Long userId) {
        log.info("Updating task with ID: {}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
        
        // Check if user is the owner of the task
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to update this task");
        }
        
        // Validate category if changed
        if (!task.getCategory().getId().equals(requestDTO.getCategoryId())) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + requestDTO.getCategoryId()));
            
            if (!category.getIsActive()) {
                throw new IllegalArgumentException("Cannot update task to inactive category");
            }
            task.setCategory(category);
        }
        
        // Update task fields
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setBudget(requestDTO.getBudget());
        task.setLocation(requestDTO.getLocation());
        task.setIsRemote(requestDTO.getIsRemote());
        task.setDueDate(requestDTO.getDueDate());
        task.setPriority(requestDTO.getPriority());
        
        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with ID: {}", updatedTask.getId());
        
        return mapToResponseDTO(updatedTask);
    }
    
    /**
     * Publish task (make it available for taskers)
     * @param id task ID
     * @param userId user ID from JWT token
     * @return updated task response
     */
    public TaskResponseDTO publishTask(Long id, Long userId) {
        log.info("Publishing task with ID: {}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
        
        // Check if user is the owner of the task
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to publish this task");
        }
        
        // Validate task can be published
        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new IllegalStateException("Only draft tasks can be published");
        }
        
        task.setStatus(TaskStatus.PUBLISHED);
        task.setPublishedAt(LocalDateTime.now());
        
        Task publishedTask = taskRepository.save(task);
        log.info("Task published successfully with ID: {}", publishedTask.getId());
        
        return mapToResponseDTO(publishedTask);
    }
    
    /**
     * Assign task to a tasker
     * @param taskId task ID
     * @param taskerId tasker ID
     * @param userId user ID from JWT token (must be task owner)
     * @return updated task response
     */
    public TaskResponseDTO assignTask(Long taskId, Long taskerId, Long userId) {
        log.info("Assigning task {} to tasker {}", taskId, taskerId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        // Check if user is the owner of the task
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to assign this task");
        }
        
        // Validate task can be assigned
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new IllegalStateException("Only published tasks can be assigned");
        }
        
        task.setAssignedTaskerId(taskerId);
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedAt(LocalDateTime.now());
        
        Task assignedTask = taskRepository.save(task);
        log.info("Task assigned successfully with ID: {}", assignedTask.getId());
        
        return mapToResponseDTO(assignedTask);
    }
    
    /**
     * Start task (tasker starts working)
     * @param taskId task ID
     * @param taskerId tasker ID from JWT token
     * @return updated task response
     */
    public TaskResponseDTO startTask(Long taskId, Long taskerId) {
        log.info("Starting task {} by tasker {}", taskId, taskerId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        // Check if user is the assigned tasker
        if (!taskerId.equals(task.getAssignedTaskerId())) {
            throw new UnauthorizedAccessException("User not authorized to start this task");
        }
        
        // Validate task can be started
        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Only assigned tasks can be started");
        }
        
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        
        Task startedTask = taskRepository.save(task);
        log.info("Task started successfully with ID: {}", startedTask.getId());
        
        return mapToResponseDTO(startedTask);
    }
    
    /**
     * Complete task (tasker marks as completed)
     * @param taskId task ID
     * @param taskerId tasker ID from JWT token
     * @return updated task response
     */
    public TaskResponseDTO completeTask(Long taskId, Long taskerId) {
        log.info("Completing task {} by tasker {}", taskId, taskerId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        // Check if user is the assigned tasker
        if (!taskerId.equals(task.getAssignedTaskerId())) {
            throw new UnauthorizedAccessException("User not authorized to complete this task");
        }
        
        // Validate task can be completed
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress tasks can be completed");
        }
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        
        Task completedTask = taskRepository.save(task);
        log.info("Task completed successfully with ID: {}", completedTask.getId());
        
        return mapToResponseDTO(completedTask);
    }
    
    /**
     * Cancel task
     * @param taskId task ID
     * @param userId user ID from JWT token (must be task owner)
     * @return updated task response
     */
    public TaskResponseDTO cancelTask(Long taskId, Long userId) {
        log.info("Cancelling task {} by user {}", taskId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        // Check if user is the owner of the task
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to cancel this task");
        }
        
        // Validate task can be cancelled
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel completed or already cancelled tasks");
        }
        
        task.setStatus(TaskStatus.CANCELLED);
        task.setCancelledAt(LocalDateTime.now());
        
        Task cancelledTask = taskRepository.save(task);
        log.info("Task cancelled successfully with ID: {}", cancelledTask.getId());
        
        return mapToResponseDTO(cancelledTask);
    }
    
    /**
     * Delete task (only draft tasks)
     * @param taskId task ID
     * @param userId user ID from JWT token (must be task owner)
     */
    public void deleteTask(Long taskId, Long userId) {
        log.info("Deleting task {} by user {}", taskId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        
        // Check if user is the owner of the task
        if (!task.getCustomerId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this task");
        }
        
        // Validate task can be deleted
        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new IllegalStateException("Only draft tasks can be deleted");
        }
        
        taskRepository.delete(task);
        log.info("Task deleted successfully with ID: {}", taskId);
    }
    
    /**
     * Search tasks by text query
     * @param query search query
     * @param pageable pagination information
     * @return page of matching tasks
     */
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> searchTasks(String query, Pageable pageable) {
        log.debug("Searching tasks with query: {}", query);
        
        Page<Task> taskPage = taskRepository.searchTasks(query, pageable);
        return taskPage.map(this::mapToResponseDTO);
    }
    
    /**
     * Get tasks due soon (for notifications)
     * @param hours hours from now
     * @return list of tasks due soon
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksDueSoon(int hours) {
        log.debug("Fetching tasks due within {} hours", hours);
        
        LocalDateTime dueDate = LocalDateTime.now().plusHours(hours);
        List<TaskStatus> activeStatuses = List.of(TaskStatus.PUBLISHED, TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS);
        
        List<Task> tasks = taskRepository.findByDueDateBeforeAndStatusIn(dueDate, activeStatuses);
        return tasks.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Map Task entity to TaskResponseDTO
     * @param task task entity
     * @return task response DTO
     */
    private TaskResponseDTO mapToResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .categoryId(task.getCategory().getId())
                .categoryName(task.getCategory().getName())
                .customerId(task.getCustomerId())
                .assignedTaskerId(task.getAssignedTaskerId())
                .budget(task.getBudget())
                .location(task.getLocation())
                .isRemote(task.getIsRemote())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .publishedAt(task.getPublishedAt())
                .assignedAt(task.getAssignedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .cancelledAt(task.getCancelledAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
