package com.mshando.notificationservice.service;

import com.mshando.notificationservice.dto.EmailNotificationDTO;
import com.mshando.notificationservice.model.NotificationPriority;
import com.mshando.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailService.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "emailEnabled", false); // Disabled for testing
    }

    @Test
    void testSendEmailAsync_Success() {
        // Given
        EmailNotificationDTO emailDto = EmailNotificationDTO.builder()
                .recipientId(1L)
                .recipientEmail("recipient@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .priority(NotificationPriority.NORMAL)
                .build();

        when(notificationRepository.save(any())).thenReturn(any());

        // When
        emailService.sendEmailAsync(emailDto);

        // Then
        verify(notificationRepository, times(2)).save(any());
        verifyNoInteractions(mailSender); // Since email is disabled in test
    }

    @Test
    void testSendEmailAsync_WithTemplate() {
        // Given
        EmailNotificationDTO emailDto = EmailNotificationDTO.builder()
                .recipientId(1L)
                .recipientEmail("recipient@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .priority(NotificationPriority.HIGH)
                .templateId(1L)
                .build();

        when(notificationRepository.save(any())).thenReturn(any());
        when(templateService.processTemplate(any(), any(), any())).thenReturn("Processed Content");
        when(templateService.getTemplateSubject(any())).thenReturn("Processed Subject");

        // When
        emailService.sendEmailAsync(emailDto);

        // Then
        verify(notificationRepository, times(2)).save(any());
        verify(templateService).processTemplate(any(), any(), any());
        verify(templateService).getTemplateSubject(any());
    }
}
