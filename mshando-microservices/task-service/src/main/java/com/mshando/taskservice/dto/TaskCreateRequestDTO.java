package com.mshando.taskservice.dto;

import com.mshando.taskservice.model.enums.TaskPriority;
import com.mshando.taskservice.model.enums.TaskStatus;
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
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private TaskPriority priority;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    private BigDecimal budget;
    
    @Min(value = 1, message = "Estimated duration must be at least 1 hour")
    @Max(value = 168, message = "Estimated duration cannot exceed 168 hours (1 week)")
    private Integer estimatedDurationHours;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    private LocalDateTime dueDate;
    
    @Size(max = 1000, message = "Requirements must not exceed 1000 characters")
    private String requirements;
    
    private Boolean isRemote;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
}
