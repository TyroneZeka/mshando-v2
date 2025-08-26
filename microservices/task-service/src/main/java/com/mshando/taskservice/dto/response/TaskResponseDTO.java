package com.mshando.taskservice.dto.response;

import com.mshando.taskservice.model.enums.TaskPriority;
import com.mshando.taskservice.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Task response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
    
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long customerId;
    private Long assignedTaskerId;
    private TaskStatus status;
    private TaskPriority priority;
    private BigDecimal budget;
    private String location;
    private LocalDateTime dueDate;
    private Boolean isRemote;
    private LocalDateTime publishedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
