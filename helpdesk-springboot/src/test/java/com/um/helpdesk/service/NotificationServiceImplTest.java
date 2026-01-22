package com.um.helpdesk.service;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.NotificationRepository;
import com.um.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for Notification Module
 * Tests CRUD, Event Triggers, Escalation, and Statistics
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Module - Unit Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User adminUser;
    private User studentUser;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        // Setup Users
        adminUser = new Administrator();
        adminUser.setId(1L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@test.com");

        studentUser = new Student();
        studentUser.setId(2L);
        studentUser.setFullName("Student User");
        studentUser.setEmail("student@test.com");

        // Setup Mock Notification
        mockNotification = new Notification();
        mockNotification.setId(1L);
        mockNotification.setTitle("Test Notification");
        mockNotification.setMessage("This is a test.");
        mockNotification.setRecipient(studentUser);
        mockNotification.setPriority(NotificationPriority.NORMAL);
        mockNotification.setStatus(NotificationStatus.SENT);
        mockNotification.setCreatedAt(LocalDateTime.now());
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 1: NOTIFICATION MANAGEMENT (CRUD)
    // =================================================================

    @Test
    @DisplayName("F1: Should create a new notification")
    void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);

        Notification result = notificationService.createNotification(mockNotification);

        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("F1: Should mark notification as read")
    void testMarkAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.markAsRead(1L);

        assertTrue(mockNotification.isRead());
        assertEquals(NotificationStatus.READ, mockNotification.getStatus());
        assertNotNull(mockNotification.getReadAt());
        verify(notificationRepository).save(mockNotification);
    }

    @Test
    @DisplayName("F1: Should get unread count for user")
    void testGetUnreadCount() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(notificationRepository.countByRecipientAndIsReadFalse(studentUser)).thenReturn(5L);

        long count = notificationService.getUnreadCount(2L);

        assertEquals(5L, count);
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 2: AUTOMATED EVENT-BASED NOTIFICATIONS
    // =================================================================

    @Test
    @DisplayName("F2: Should trigger notification when Ticket is Submitted")
    void testSendTicketSubmittedNotification() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(notificationRepository.findById(any())).thenReturn(Optional.of(mockNotification)); // Stub for
                                                                                                // sendNotification
                                                                                                // retrieval
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = (Notification) i.getArguments()[0];
            n.setId(100L); // Simulate DB ID generation
            return n;
        });

        notificationService.sendTicketSubmittedNotification(999L, 2L);

        // Verification
        // 1. Should save the new notification
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        // 2. Should attempt to persist it (create) and then update it (send)
    }

    @Test
    @DisplayName("F2: Should trigger notification when Ticket is Assigned")
    void testSendTicketAssignedNotification() {
        User technician = new TechnicianSupportStaff();
        technician.setId(3L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(technician));
        when(notificationRepository.findById(any())).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = (Notification) i.getArguments()[0];
            n.setId(101L);
            return n;
        });

        notificationService.sendTicketAssignedNotification(555L, 3L);

        verify(notificationRepository, atLeastOnce())
                .save(argThat(n -> n.getType() == NotificationType.TICKET_ASSIGNED &&
                        n.getPriority() == NotificationPriority.HIGH));
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 3: REMINDER & ESCALATION SYSTEM
    // =================================================================

    @Test
    @DisplayName("F3: Should send Urgent Reminder Notification")
    void testSendReminderNotification() {
        User technician = new TechnicianSupportStaff();
        technician.setId(3L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(technician));
        when(notificationRepository.findById(any())).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = (Notification) i.getArguments()[0];
            n.setId(102L);
            return n;
        });

        notificationService.sendReminderNotification(123L, 3L);

        verify(notificationRepository, atLeastOnce()).save(argThat(n -> n.getTitle().contains("Reminder") &&
                n.getPriority() == NotificationPriority.URGENT));
    }

    @Test
    @DisplayName("F3: Should send Escalation Notification to Supervisor")
    void testSendEscalationNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(notificationRepository.findById(any())).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = (Notification) i.getArguments()[0];
            n.setId(103L);
            return n;
        });

        notificationService.sendEscalationNotification(123L, 1L, 2); // Level 2 Escalation

        verify(notificationRepository, atLeastOnce()).save(argThat(n -> n.getType() == NotificationType.ESCALATION &&
                n.getMessage().contains("Supervisor level")));
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 4: DELIVERY STATISTICS
    // =================================================================

    @Test
    @DisplayName("F4: Should calculate delivery statistics correctly")
    void testGetDeliveryStatistics() {
        // Arrange: Create a list of notifications with different statuses
        Notification n1 = new Notification();
        n1.setStatus(NotificationStatus.DELIVERED);
        Notification n2 = new Notification();
        n2.setStatus(NotificationStatus.READ);
        Notification n3 = new Notification();
        n3.setStatus(NotificationStatus.FAILED);
        Notification n4 = new Notification();
        n4.setStatus(NotificationStatus.SENT);

        List<Notification> list = Arrays.asList(n1, n2, n3, n4);

        when(notificationRepository.findAll()).thenReturn(list);

        // Act
        String stats = notificationService.getDeliveryStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.contains("Total Notifications: 4"));
        assertTrue(stats.contains("Delivered: 1"));
        assertTrue(stats.contains("Read: 1"));
        assertTrue(stats.contains("Failed: 1"));
    }
}
