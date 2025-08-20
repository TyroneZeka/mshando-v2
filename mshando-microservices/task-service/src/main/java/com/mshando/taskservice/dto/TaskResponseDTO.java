package com.mshando.taskservice.dto;

import com.mshando.taskservice.model.enums.TaskPriority;
import com.mshando.taskservice.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private CategoryResponseDTO category;
    private Long customerId;
    private Long assignedTaskerId;
    private TaskStatus status;
    private TaskPriority priority;
    private BigDecimal budget;
    private Integer estimatedDurationHours;
    private String location;
    private LocalDateTime dueDate;
    private List<TaskImageResponseDTO> images;
    private String requirements;
    private Boolean isRemote;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
