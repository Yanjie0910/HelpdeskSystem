package com.um.helpdesk.notification.impl;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OSGi Notification Component Tests")
class NotificationOsgiTest {

    @Mock
    private EntityManager em;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EntityTransaction transaction;

    private NotificationServiceImpl notificationService; // Cannot use @InjectMocks directly due to manual constructor

    private User adminUser;
    private User recipientUser;

    @BeforeEach
    void setUp() throws Exception {
        // Setup Transaction Mock
        lenient().when(em.getTransaction()).thenReturn(transaction);

        // Manually instantiate service because it creates its own repo in constructor
        notificationService = new NotificationServiceImpl(em);

        // Use Reflection to inject our Mock Repository (override the one created in
        // constructor)
        Field repoField = NotificationServiceImpl.class.getDeclaredField("notificationRepository");
        repoField.setAccessible(true);
        repoField.set(notificationService, notificationRepository);

        // Setup Test Data
        adminUser = new Administrator();
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);

        recipientUser = new Student();
        recipientUser.setId(2L);
        recipientUser.setFullName("Test Student");
    }

    // =================================================================
    // 🧪 TEST 1: CREATE NOTIFICATION (Logic + Transaction)
    // =================================================================
    @Test
    void testCreateNotification_ShouldUseTransaction() {
        Notification n = new Notification();
        n.setTitle("OSGi Test");

        when(notificationRepository.save(any(Notification.class))).thenReturn(n);

        notificationService.createNotification(n);

        // Verify Transaction Boundaries for OSGi Manual Logic
        verify(transaction).begin();
        verify(notificationRepository).save(n);
        verify(transaction).commit();
    }

    // =================================================================
    // 🧪 TEST 2: EVENT TRIGGER (Ticket Submitted)
    // =================================================================
    @Test
    void testSendTicketSubmitted_ShouldFindUserAndNotify() {
        // Mock Finding User
        when(em.find(User.class, 2L)).thenReturn(recipientUser);

        // Mock finding the new notification to send it (must return an object with
        // title!)
        Notification existingNotif = new Notification();
        existingNotif.setId(100L);
        existingNotif.setTitle("Draft");
        when(notificationRepository.findById(any())).thenReturn(existingNotif);

        // Mock Save - simplistic return
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = (Notification) i.getArguments()[0];
            if (n.getId() == null)
                n.setId(100L); // Simulate generated ID
            return n;
        });

        notificationService.sendTicketSubmittedNotification(999L, 2L);

        // Verify Flow
        verify(em).find(User.class, 2L);
        verify(notificationRepository, atLeastOnce()).save(argThat(notif -> notif.getTitle() != null &&
                notif.getTitle().contains("Submitted Successfully") &&
                notif.getType() == NotificationType.TICKET_SUBMITTED));
    }

    // =================================================================
    // 🧪 TEST 3: ESCALATION (Urgent Priority)
    // =================================================================
    @Test
    void testSendEscalation_ShouldBeUrgent() {
        when(em.find(User.class, 1L)).thenReturn(adminUser);
        when(notificationRepository.findById(any())).thenReturn(new Notification());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.sendEscalationNotification(55L, 1L, 3); // Level 3

        verify(notificationRepository, atLeastOnce())
                .save(argThat(notif -> notif.getPriority() == NotificationPriority.URGENT &&
                        notif.getType() == NotificationType.ESCALATION));
    }

    // =================================================================
    // 🧪 TEST 4: DELIVERY STATS (Math Check)
    // =================================================================
    @Test
    void testDeliveryStats_ShouldFormatString() {
        Notification n1 = new Notification();
        n1.setStatus(NotificationStatus.SENT);
        Notification n2 = new Notification();
        n2.setStatus(NotificationStatus.FAILED);

        when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

        String stats = notificationService.getDeliveryStatistics();

        assertTrue(stats.contains("Total Notifications: 2"));
        assertTrue(stats.contains("Sent: 1"));
        assertTrue(stats.contains("Failed: 1"));
        assertTrue(stats.contains("(OSGi)")); // Check for specific OSGi label
    }
}
