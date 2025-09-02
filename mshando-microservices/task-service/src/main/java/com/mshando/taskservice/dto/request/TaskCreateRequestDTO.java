package com.mshando.taskservice.dto.request;

import com.mshando.taskservice.model.enums.TaskPriority;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Task creation request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequestDTO {
    
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Task description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 1000, message = "Requirements description must not exceed 1000 characters")
    private String requirementsDescription;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private TaskPriority priority;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    private BigDecimal budget;
    
    @Min(value = 1, message = "Estimated duration must be at least 1 hour")
    @Max(value = 168, message = "Estimated duration cannot exceed 168 hours (1 week)")
    private Integer estimatedDuration;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    private LocalDateTime dueDate;
    
    private Boolean isRemote;
}
