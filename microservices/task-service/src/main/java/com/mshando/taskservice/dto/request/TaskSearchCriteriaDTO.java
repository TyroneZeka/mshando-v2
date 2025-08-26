package com.mshando.taskservice.dto.request;

import com.mshando.taskservice.model.enums.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Task search criteria DTO for advanced filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchCriteriaDTO {
    
    private String query;
    private Long categoryId;
    private TaskPriority priority;
    private BigDecimal minBudget;
    private BigDecimal maxBudget;
    private String location;
    private Boolean isRemote;
    private Boolean includeCompleted;
    private String sortBy;
    private String sortDirection;
}
