package com.mshando.taskservice.service;

import com.mshando.taskservice.dto.request.TaskCreateRequestDTO;
import com.mshando.taskservice.dto.response.TaskResponseDTO;
import com.mshando.taskservice.exception.CategoryNotFoundException;
import com.mshando.taskservice.exception.TaskNotFoundException;
import com.mshando.taskservice.exception.UnauthorizedAccessException;
import com.mshando.taskservice.model.Category;
import com.mshando.taskservice.model.Task;
import com.mshando.taskservice.model.enums.TaskPriority;
import com.mshando.taskservice.model.enums.TaskStatus;
import com.mshando.taskservice.repository.CategoryRepository;
import com.mshando.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private Category testCategory;
    private TaskCreateRequestDTO createRequestDTO;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Home Cleaning")
                .description("Professional home cleaning services")
                .isActive(true)
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Clean my house")
                .description("Need someone to clean my 3-bedroom house")
                .category(testCategory)
                .customerId(100L)
                .status(TaskStatus.DRAFT)
                .priority(TaskPriority.MEDIUM)
                .budget(new BigDecimal("150.00"))
                .location("123 Main St, City")
                .isRemote(false)
                .dueDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequestDTO = TaskCreateRequestDTO.builder()
                .title("Clean my house")
                .description("Need someone to clean my 3-bedroom house")
                .categoryId(1L)
                .priority(TaskPriority.MEDIUM)
                .budget(new BigDecimal("150.00"))
                .location("123 Main St, City")
                .isRemote(false)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void createTask_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponseDTO result = taskService.createTask(createRequestDTO, 100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean my house");
        assertThat(result.getCustomerId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.DRAFT);

        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void createTask_CategoryNotFound_ThrowsException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(createRequestDTO, 100L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with ID: 1");

        verify(categoryRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when category is inactive")
    void createTask_InactiveCategory_ThrowsException() {
        // Given
        testCategory.setIsActive(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(createRequestDTO, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create task for inactive category");

        verify(categoryRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void getTaskById_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponseDTO result = taskService.getTaskById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean my house");

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void getTaskById_NotFound_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with ID: 1");

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get tasks by customer ID successfully")
    void getTasksByCustomerId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(testTask));
        when(taskRepository.findByCustomerId(100L, pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponseDTO> result = taskService.getTasksByCustomerId(100L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(100L);

        verify(taskRepository).findByCustomerId(100L, pageable);
    }

    @Test
    @DisplayName("Should publish task successfully")
    void publishTask_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponseDTO result = taskService.publishTask(1L, 100L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(argThat(task -> 
            task.getStatus() == TaskStatus.PUBLISHED && 
            task.getPublishedAt() != null
        ));
    }

    @Test
    @DisplayName("Should throw exception when publishing task with wrong owner")
    void publishTask_WrongOwner_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskService.publishTask(1L, 200L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not authorized to publish this task");

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when publishing non-draft task")
    void publishTask_NotDraft_ThrowsException() {
        // Given
        testTask.setStatus(TaskStatus.PUBLISHED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskService.publishTask(1L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft tasks can be published");

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should assign task successfully")
    void assignTask_Success() {
        // Given
        testTask.setStatus(TaskStatus.PUBLISHED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponseDTO result = taskService.assignTask(1L, 200L, 100L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(argThat(task -> 
            task.getStatus() == TaskStatus.ASSIGNED && 
            task.getAssignedTaskerId().equals(200L) &&
            task.getAssignedAt() != null
        ));
    }

    @Test
    @DisplayName("Should start task successfully")
    void startTask_Success() {
        // Given
        testTask.setStatus(TaskStatus.ASSIGNED);
        testTask.setAssignedTaskerId(200L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponseDTO result = taskService.startTask(1L, 200L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(argThat(task -> 
            task.getStatus() == TaskStatus.IN_PROGRESS && 
            task.getStartedAt() != null
        ));
    }

    @Test
    @DisplayName("Should throw exception when starting task with wrong tasker")
    void startTask_WrongTasker_ThrowsException() {
        // Given
        testTask.setStatus(TaskStatus.ASSIGNED);
        testTask.setAssignedTaskerId(200L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskService.startTask(1L, 300L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not authorized to start this task");

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should complete task successfully")
    void completeTask_Success() {
        // Given
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        testTask.setAssignedTaskerId(200L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponseDTO result = taskService.completeTask(1L, 200L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(argThat(task -> 
            task.getStatus() == TaskStatus.COMPLETED && 
            task.getCompletedAt() != null
        ));
    }

    @Test
    @DisplayName("Should cancel task successfully")
    void cancelTask_Success() {
        // Given
        testTask.setStatus(TaskStatus.PUBLISHED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponseDTO result = taskService.cancelTask(1L, 100L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(argThat(task -> 
            task.getStatus() == TaskStatus.CANCELLED && 
            task.getCancelledAt() != null
        ));
    }

    @Test
    @DisplayName("Should delete draft task successfully")
    void deleteTask_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        taskService.deleteTask(1L, 100L);

        // Then
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(testTask);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-draft task")
    void deleteTask_NotDraft_ThrowsException() {
        // Given
        testTask.setStatus(TaskStatus.PUBLISHED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(1L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft tasks can be deleted");

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("Should search published tasks with filters")
    void searchPublishedTasks_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(testTask));
        when(taskRepository.findPublishedTasksWithFilters(
            eq(1L), any(BigDecimal.class), any(BigDecimal.class), 
            eq("City"), eq(false), eq(pageable)
        )).thenReturn(taskPage);

        // When
        Page<TaskResponseDTO> result = taskService.searchPublishedTasks(
            1L, new BigDecimal("100"), new BigDecimal("200"), 
            "City", false, pageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(taskRepository).findPublishedTasksWithFilters(
            eq(1L), any(BigDecimal.class), any(BigDecimal.class), 
            eq("City"), eq(false), eq(pageable)
        );
    }

    @Test
    @DisplayName("Should get tasks due soon")
    void getTasksDueSoon_Success() {
        // Given
        testTask.setStatus(TaskStatus.PUBLISHED);
        testTask.setDueDate(LocalDateTime.now().plusHours(12));
        List<Task> dueTasks = Arrays.asList(testTask);
        when(taskRepository.findByDueDateBeforeAndStatusIn(
            any(LocalDateTime.class), anyList()
        )).thenReturn(dueTasks);

        // When
        List<TaskResponseDTO> result = taskService.getTasksDueSoon(24);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(taskRepository).findByDueDateBeforeAndStatusIn(
            any(LocalDateTime.class), anyList()
        );
    }
}
