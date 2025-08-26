package com.mshando.taskservice.service;

import com.mshando.taskservice.dto.response.TaskImageResponseDTO;
import com.mshando.taskservice.exception.TaskNotFoundException;
import com.mshando.taskservice.exception.UnauthorizedAccessException;
import com.mshando.taskservice.model.Task;
import com.mshando.taskservice.model.TaskImage;
import com.mshando.taskservice.model.enums.TaskStatus;
import com.mshando.taskservice.repository.TaskImageRepository;
import com.mshando.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskImageService Unit Tests")
class TaskImageServiceTest {

    @Mock
    private TaskImageRepository taskImageRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskImageService taskImageService;

    @TempDir
    Path tempDir;

    private Task testTask;
    private TaskImage testTaskImage;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .customerId(100L)
                .status(TaskStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        testTaskImage = TaskImage.builder()
                .id(1L)
                .task(testTask)
                .fileName("test-image.jpg")
                .originalFileName("original-test.jpg")
                .filePath("/uploads/tasks/1/test-image.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .isPrimary(false)
                .createdAt(LocalDateTime.now())
                .build();

        testFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Set upload directory for testing using reflection
        ReflectionTestUtils.setField(taskImageService, "uploadDirectory", tempDir.toString());
        ReflectionTestUtils.setField(taskImageService, "maxImagesPerTask", 5);
        ReflectionTestUtils.setField(taskImageService, "maxFileSize", 5242880L);
    }

    @Test
    @DisplayName("Should upload image successfully")
    void uploadImage_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.countByTaskId(1L)).thenReturn(0L);
        when(taskImageRepository.save(any(TaskImage.class))).thenReturn(testTaskImage);

        // When
        TaskImageResponseDTO result = taskImageService.uploadTaskImage(1L, testFile, 100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFileName()).isEqualTo("test-image.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository).countByTaskId(1L);
        verify(taskImageRepository).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void uploadImage_TaskNotFound_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskImageService.uploadTaskImage(1L, testFile, 100L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with ID: 1");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception when user not authorized")
    void uploadImage_UnauthorizedUser_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskImageService.uploadTaskImage(1L, testFile, 200L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not authorized to upload images for this task");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid file type")
    void uploadImage_InvalidFileType_ThrowsException() {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskImageService.uploadTaskImage(1L, invalidFile, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file type");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception for file too large")
    void uploadImage_FileTooLarge_ThrowsException() {
        // Given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskImageService.uploadTaskImage(1L, largeFile, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds maximum allowed size");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception when max images reached")
    void uploadImage_MaxImagesReached_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.countByTaskId(1L)).thenReturn(5L);

        // When & Then
        assertThatThrownBy(() -> taskImageService.uploadTaskImage(1L, testFile, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum 5 images allowed per task");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository).countByTaskId(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should get images by task ID successfully")
    void getTaskImages_Success() {
        // Given
        List<TaskImage> images = Arrays.asList(testTaskImage);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskImageRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(images);

        // When
        List<TaskImageResponseDTO> result = taskImageService.getTaskImages(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getFileName()).isEqualTo("test-image.jpg");

        verify(taskRepository).existsById(1L);
        verify(taskImageRepository).findByTaskIdOrderByCreatedAtAsc(1L);
    }

    @Test
    @DisplayName("Should throw exception when getting images for non-existent task")
    void getTaskImages_TaskNotFound_ThrowsException() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskImageService.getTaskImages(1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with ID: 1");

        verify(taskRepository).existsById(1L);
        verify(taskImageRepository, never()).findByTaskIdOrderByCreatedAtAsc(anyLong());
    }

    @Test
    @DisplayName("Should get primary image successfully")
    void getPrimaryImage_Success() {
        // Given
        when(taskImageRepository.findPrimaryImageByTaskId(1L)).thenReturn(testTaskImage);

        // When
        TaskImageResponseDTO result = taskImageService.getPrimaryImage(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFileName()).isEqualTo("test-image.jpg");

        verify(taskImageRepository).findPrimaryImageByTaskId(1L);
    }

    @Test
    @DisplayName("Should return null when no primary image exists")
    void getPrimaryImage_NoPrimary_ReturnsNull() {
        // Given
        when(taskImageRepository.findPrimaryImageByTaskId(1L)).thenReturn(null);

        // When
        TaskImageResponseDTO result = taskImageService.getPrimaryImage(1L);

        // Then
        assertThat(result).isNull();

        verify(taskImageRepository).findPrimaryImageByTaskId(1L);
    }

    @Test
    @DisplayName("Should set primary image successfully")
    void setPrimaryImage_Success() {
        // Given
        TaskImage currentPrimary = TaskImage.builder()
                .id(2L)
                .task(testTask)
                .isPrimary(true)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.findById(1L)).thenReturn(Optional.of(testTaskImage));
        when(taskImageRepository.findPrimaryImageByTaskId(1L)).thenReturn(currentPrimary);
        when(taskImageRepository.save(any(TaskImage.class))).thenReturn(testTaskImage);

        // When
        TaskImageResponseDTO result = taskImageService.setPrimaryImage(1L, 1L, 100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(taskRepository).findById(1L);
        verify(taskImageRepository).findById(1L);
        verify(taskImageRepository).findPrimaryImageByTaskId(1L);
        verify(taskImageRepository, times(2)).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception when image not found for set primary")
    void setPrimaryImage_ImageNotFound_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskImageService.setPrimaryImage(1L, 1L, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image not found with ID: 1");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository).findById(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception when image belongs to different task")
    void setPrimaryImage_ImageBelongsToDifferentTask_ThrowsException() {
        // Given
        Task differentTask = Task.builder().id(2L).customerId(100L).build();
        testTaskImage.setTask(differentTask);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.findById(1L)).thenReturn(Optional.of(testTaskImage));

        // When & Then
        assertThatThrownBy(() -> taskImageService.setPrimaryImage(1L, 1L, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image does not belong to the specified task");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository).findById(1L);
        verify(taskImageRepository, never()).save(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should delete image successfully")
    void deleteTaskImage_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.findById(1L)).thenReturn(Optional.of(testTaskImage));

        // When
        taskImageService.deleteTaskImage(1L, 1L, 100L);

        // Then
        verify(taskRepository).findById(1L);
        verify(taskImageRepository).findById(1L);
        verify(taskImageRepository).delete(testTaskImage);
    }

    @Test
    @DisplayName("Should throw exception when deleting image not found")
    void deleteTaskImage_ImageNotFound_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskImageService.deleteTaskImage(1L, 1L, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image not found with ID: 1");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository).findById(1L);
        verify(taskImageRepository, never()).delete(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should throw exception when user not authorized to delete")
    void deleteTaskImage_UnauthorizedUser_ThrowsException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskImageService.deleteTaskImage(1L, 1L, 200L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not authorized to delete images for this task");

        verify(taskRepository).findById(1L);
        verify(taskImageRepository, never()).findById(anyLong());
        verify(taskImageRepository, never()).delete(any(TaskImage.class));
    }

    @Test
    @DisplayName("Should delete all task images successfully")
    void deleteAllTaskImages_Success() {
        // Given
        List<TaskImage> images = Arrays.asList(testTaskImage);
        when(taskImageRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(images);

        // When
        taskImageService.deleteAllTaskImages(1L);

        // Then
        verify(taskImageRepository).findByTaskIdOrderByCreatedAtAsc(1L);
        verify(taskImageRepository).deleteByTaskId(1L);
    }

    @Test
    @DisplayName("Should validate file type correctly")
    void validateFile_ValidTypes() {
        // Test valid image types
        MockMultipartFile jpegFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile pngFile = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());
        MockMultipartFile gifFile = new MockMultipartFile("file", "test.gif", "image/gif", "content".getBytes());
        MockMultipartFile webpFile = new MockMultipartFile("file", "test.webp", "image/webp", "content".getBytes());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskImageRepository.countByTaskId(1L)).thenReturn(0L);
        when(taskImageRepository.save(any(TaskImage.class))).thenReturn(testTaskImage);

        // These should not throw exceptions
        assertThatCode(() -> taskImageService.uploadTaskImage(1L, jpegFile, 100L)).doesNotThrowAnyException();
        assertThatCode(() -> taskImageService.uploadTaskImage(1L, pngFile, 100L)).doesNotThrowAnyException();
        assertThatCode(() -> taskImageService.uploadTaskImage(1L, gifFile, 100L)).doesNotThrowAnyException();
        assertThatCode(() -> taskImageService.uploadTaskImage(1L, webpFile, 100L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject empty file")
    void validateFile_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThatThrownBy(() -> taskImageService.uploadTaskImage(1L, emptyFile, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File cannot be empty");
    }
}

