package com.um.helpdesk.service;

import com.um.helpdesk.entity.DeliveryChannel;
import com.um.helpdesk.entity.Notification;
import java.util.List;

public interface NotificationService {

    // ========== FUNCTIONALITY 1: Notification Management (CRUD) ==========

    Notification createNotification(Notification notification);
    List<Notification> getAllNotifications();
    Notification getNotificationById(Long id);
    List<Notification> getNotificationsByUser(Long userId);
    List<Notification> getUnreadNotifications(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    Notification updateNotification(Long id, Notification notification);
    void deleteNotification(Long id);
    long getUnreadCount(Long userId);

    // ========== FUNCTIONALITY 2: Automated Event-Based Notifications ==========

    void sendTicketSubmittedNotification(Long ticketId, Long userId);
    void sendTicketAssignedNotification(Long ticketId, Long technicianId);
    void sendTicketStatusChangedNotification(Long ticketId, String oldStatus, String newStatus);
    void sendTicketResolvedNotification(Long ticketId, Long userId);
    void sendTicketReopenedNotification(Long ticketId, Long technicianId);

    // ========== FUNCTIONALITY 3: Reminder & Escalation System ==========

    void sendReminderNotification(Long ticketId, Long technicianId);
    void sendEscalationNotification(Long ticketId, Long supervisorId, int escalationLevel);
    void checkAndProcessOverdueTickets();

    // ========== FUNCTIONALITY 4: Notification Delivery Management ==========

    void sendNotification(Long notificationId, DeliveryChannel channel);
    void updateDeliveryStatus(Long notificationId, String status);
    void retryFailedDeliveries();
    String getDeliveryStatistics();
}

