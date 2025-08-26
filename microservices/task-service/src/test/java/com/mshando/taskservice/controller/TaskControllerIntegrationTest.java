package com.mshando.taskservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mshando.taskservice.dto.request.TaskCreateRequestDTO;
import com.mshando.taskservice.model.Category;
import com.mshando.taskservice.model.Task;
import com.mshando.taskservice.model.enums.TaskPriority;
import com.mshando.taskservice.model.enums.TaskStatus;
import com.mshando.taskservice.repository.CategoryRepository;
import com.mshando.taskservice.repository.TaskRepository;
import com.mshando.taskservice.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TaskController Integration Tests")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private Category testCategory;
    private Task testTask;
    private String customerToken;
    private String taskerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Clean database
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        testCategory = Category.builder()
                .name("Home Cleaning")
                .description("Professional home cleaning services")
                .isActive(true)
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test task
        testTask = Task.builder()
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
                .build();
        testTask = taskRepository.save(testTask);

        // Generate JWT tokens for testing
        customerToken = jwtTokenUtil.generateToken("customer@test.com", 100L, List.of("CUSTOMER"));
        taskerToken = jwtTokenUtil.generateToken("tasker@test.com", 200L, List.of("TASKER"));
        adminToken = jwtTokenUtil.generateToken("admin@test.com", 1L, List.of("ADMIN"));
    }

    @Test
    @DisplayName("Should create task successfully")
    void createTask_Success() throws Exception {
        TaskCreateRequestDTO createRequest = TaskCreateRequestDTO.builder()
                .title("New Task")
                .description("Task description")
                .categoryId(testCategory.getId())
                .priority(TaskPriority.HIGH)
                .budget(new BigDecimal("200.00"))
                .location("456 Oak St, City")
                .isRemote(false)
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task description"))
                .andExpect(jsonPath("$.customerId").value(100L))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.budget").value(200.00))
                .andExpect(jsonPath("$.location").value("456 Oak St, City"))
                .andExpect(jsonPath("$.isRemote").value(false));
    }

    @Test
    @DisplayName("Should return 401 when creating task without token")
    void createTask_NoToken_Returns401() throws Exception {
        TaskCreateRequestDTO createRequest = TaskCreateRequestDTO.builder()
                .title("New Task")
                .description("Task description")
                .categoryId(testCategory.getId())
                .priority(TaskPriority.HIGH)
                .budget(new BigDecimal("200.00"))
                .location("456 Oak St, City")
                .isRemote(false)
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when creating task with invalid data")
    void createTask_InvalidData_Returns400() throws Exception {
        TaskCreateRequestDTO createRequest = TaskCreateRequestDTO.builder()
                .title("") // Empty title
                .description("Task description")
                .categoryId(testCategory.getId())
                .priority(TaskPriority.HIGH)
                .budget(new BigDecimal("-100.00")) // Negative budget
                .location("456 Oak St, City")
                .isRemote(false)
                .dueDate(LocalDateTime.now().minusDays(1)) // Past due date
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void getTaskById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value("Clean my house"))
                .andExpect(jsonPath("$.customerId").value(100L))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("Should return 404 when task not found")
    void getTaskById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}", 999L)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get tasks by customer ID successfully")
    void getTasksByCustomerId_Success() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/customer/{customerId}", 100L)
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customerId").value(100L));
    }

    @Test
    @DisplayName("Should publish task successfully")
    void publishTask_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/{id}/publish", testTask.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 403 when non-owner tries to publish task")
    void publishTask_NonOwner_Returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/{id}/publish", testTask.getId())
                        .header("Authorization", "Bearer " + taskerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should assign task successfully")
    void assignTask_Success() throws Exception {
        // First publish the task
        testTask.setStatus(TaskStatus.PUBLISHED);
        testTask.setPublishedAt(LocalDateTime.now());
        taskRepository.save(testTask);

        mockMvc.perform(patch("/api/v1/tasks/{id}/assign", testTask.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .param("taskerId", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTaskerId").value(200L))
                .andExpect(jsonPath("$.assignedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should start task successfully")
    void startTask_Success() throws Exception {
        // First assign the task
        testTask.setStatus(TaskStatus.ASSIGNED);
        testTask.setAssignedTaskerId(200L);
        testTask.setAssignedAt(LocalDateTime.now());
        taskRepository.save(testTask);

        mockMvc.perform(patch("/api/v1/tasks/{id}/start", testTask.getId())
                        .header("Authorization", "Bearer " + taskerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should complete task successfully")
    void completeTask_Success() throws Exception {
        // First start the task
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        testTask.setAssignedTaskerId(200L);
        testTask.setStartedAt(LocalDateTime.now());
        taskRepository.save(testTask);

        mockMvc.perform(patch("/api/v1/tasks/{id}/complete", testTask.getId())
                        .header("Authorization", "Bearer " + taskerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should cancel task successfully")
    void cancelTask_Success() throws Exception {
        // Publish the task first
        testTask.setStatus(TaskStatus.PUBLISHED);
        testTask.setPublishedAt(LocalDateTime.now());
        taskRepository.save(testTask);

        mockMvc.perform(patch("/api/v1/tasks/{id}/cancel", testTask.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should delete draft task successfully")
    void deleteTask_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search published tasks successfully")
    void searchPublishedTasks_Success() throws Exception {
        // Create a published task
        Task publishedTask = Task.builder()
                .title("Another cleaning task")
                .description("Different cleaning task")
                .category(testCategory)
                .customerId(300L)
                .status(TaskStatus.PUBLISHED)
                .priority(TaskPriority.HIGH)
                .budget(new BigDecimal("180.00"))
                .location("789 Pine St, City")
                .isRemote(false)
                .dueDate(LocalDateTime.now().plusDays(10))
                .publishedAt(LocalDateTime.now())
                .build();
        taskRepository.save(publishedTask);

        mockMvc.perform(get("/api/v1/tasks/search")
                        .header("Authorization", "Bearer " + taskerToken)
                        .param("categoryId", testCategory.getId().toString())
                        .param("minBudget", "100")
                        .param("maxBudget", "300")
                        .param("location", "City")
                        .param("isRemote", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PUBLISHED"))));
    }

    @Test
    @DisplayName("Should get tasks due soon successfully")
    void getTasksDueSoon_Success() throws Exception {
        // Create a task due soon
        Task dueSoonTask = Task.builder()
                .title("Urgent task")
                .description("Task due very soon")
                .category(testCategory)
                .customerId(100L)
                .status(TaskStatus.PUBLISHED)
                .priority(TaskPriority.HIGH)
                .budget(new BigDecimal("250.00"))
                .location("urgent location")
                .isRemote(false)
                .dueDate(LocalDateTime.now().plusHours(12))
                .publishedAt(LocalDateTime.now())
                .build();
        taskRepository.save(dueSoonTask);

        mockMvc.perform(get("/api/v1/tasks/due-soon")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("hours", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should return 403 when non-admin tries to get tasks due soon")
    void getTasksDueSoon_NonAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/due-soon")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("hours", "24"))
                .andExpect(status().isForbidden());
    }
}
