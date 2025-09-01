package com.mshando.taskservice.controller;

import com.mshando.taskservice.dto.request.TaskCreateRequestDTO;
import com.mshando.taskservice.dto.response.TaskResponseDTO;
import com.mshando.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Task management
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Management", description = "APIs for managing tasks throughout their lifecycle")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create a new task", description = "Create a new task (Customer only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Customer access required"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskCreateRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating new task for user: {}", userDetails.getUsername());

        Long customerId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.createTask(requestDTO, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        log.debug("Fetching task with ID: {}", id);

        TaskResponseDTO responseDTO = taskService.getTaskById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Get my tasks", description = "Get tasks created by the authenticated customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<TaskResponseDTO>> getMyTasks(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching tasks for customer: {}", userDetails.getUsername());

        Long customerId = extractUserIdFromUserDetails(userDetails);
        Page<TaskResponseDTO> taskPage = taskService.getTasksByCustomerId(customerId, pageable);
        return ResponseEntity.ok(taskPage);
    }

    @Operation(summary = "Get my assigned tasks", description = "Get tasks assigned to the authenticated tasker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<Page<TaskResponseDTO>> getMyAssignments(
            @PageableDefault(size = 20, sort = "assignedAt") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Fetching assigned tasks for tasker: {}", userDetails.getUsername());

        Long taskerId = extractUserIdFromUserDetails(userDetails);
        Page<TaskResponseDTO> taskPage = taskService.getTasksByTaskerId(taskerId, pageable);
        return ResponseEntity.ok(taskPage);
    }

    @Operation(summary = "Search published tasks", description = "Search and filter published tasks available for assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchPublishedTasks(
            @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum budget") @RequestParam(required = false) BigDecimal minBudget,
            @Parameter(description = "Maximum budget") @RequestParam(required = false) BigDecimal maxBudget,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Remote work only") @RequestParam(required = false) Boolean isRemote,
            @PageableDefault(size = 20, sort = "published_at") Pageable pageable) {
        log.debug("Searching published tasks with filters");

        Page<TaskResponseDTO> taskPage = taskService.searchPublishedTasks(
                categoryId, minBudget, maxBudget, location, isRemote, pageable);
        return ResponseEntity.ok(taskPage);
    }

    @Operation(summary = "Text search tasks", description = "Search tasks by title or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/search/text")
    public ResponseEntity<Page<TaskResponseDTO>> searchTasksByText(
            @Parameter(description = "Search query") @RequestParam String q,
            @PageableDefault(size = 20, sort = "publishedAt") Pageable pageable) {
        log.debug("Text searching tasks with query: {}", q);

        Page<TaskResponseDTO> taskPage = taskService.searchTasks(q, pageable);
        return ResponseEntity.ok(taskPage);
    }

    @Operation(summary = "Update task", description = "Update an existing task (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskCreateRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating task with ID: {} by user: {}", id, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.updateTask(id, requestDTO, userId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Publish task", description = "Publish a draft task to make it available for taskers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task published successfully"),
            @ApiResponse(responseCode = "400", description = "Task cannot be published"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponseDTO> publishTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Publishing task with ID: {} by user: {}", id, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.publishTask(id, userId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Assign task", description = "Assign a published task to a tasker (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Task cannot be assigned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponseDTO> assignTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "Tasker ID") @RequestParam Long taskerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Assigning task {} to tasker {} by user: {}", id, taskerId, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.assignTask(id, taskerId, userId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Start task", description = "Start working on an assigned task (Assigned tasker only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task started successfully"),
            @ApiResponse(responseCode = "400", description = "Task cannot be started"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Assigned tasker access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<TaskResponseDTO> startTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Starting task with ID: {} by tasker: {}", id, userDetails.getUsername());

        Long taskerId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.startTask(id, taskerId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Complete task", description = "Mark task as completed (Assigned tasker only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task completed successfully"),
            @ApiResponse(responseCode = "400", description = "Task cannot be completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Assigned tasker access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<TaskResponseDTO> completeTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Completing task with ID: {} by tasker: {}", id, userDetails.getUsername());

        Long taskerId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.completeTask(id, taskerId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Cancel task", description = "Cancel a task (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Task cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponseDTO> cancelTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Cancelling task with ID: {} by user: {}", id, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        TaskResponseDTO responseDTO = taskService.cancelTask(id, userId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Delete task", description = "Delete a draft task (Task owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Task cannot be deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Task owner access required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting task with ID: {} by user: {}", id, userDetails.getUsername());

        Long userId = extractUserIdFromUserDetails(userDetails);
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get tasks due soon", description = "Get tasks that are due within specified hours (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/due-soon")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponseDTO>> getTasksDueSoon(
            @Parameter(description = "Hours from now") @RequestParam(defaultValue = "24") int hours) {
        log.debug("Fetching tasks due within {} hours", hours);

        List<TaskResponseDTO> tasks = taskService.getTasksDueSoon(hours);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Extract user ID from UserDetails (assuming the username is the user ID)
     * This will be properly implemented once we integrate with the user service
     */
    private Long extractUserIdFromUserDetails(UserDetails userDetails) {
        // TODO: Implement proper user ID extraction from JWT token
        // For now, assuming username contains the user ID
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            log.warn("Unable to extract user ID from username: {}", userDetails.getUsername());
            return 1L; // Default fallback - will be replaced with proper JWT implementation
        }
    }
}
