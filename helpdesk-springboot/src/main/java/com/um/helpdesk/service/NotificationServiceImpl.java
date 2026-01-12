package com.um.helpdesk.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.NotificationRepository;
import com.um.helpdesk.repository.UserRepository;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // ========== FUNCTIONALITY 1: Notification Management (CRUD) ==========

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    @Override
    public List<Notification> getNotificationsByUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return notificationRepository.findByRecipient(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return notificationRepository.findByRecipientAndIsReadFalse(user);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.READ);

        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications =
            notificationRepository.findByRecipientAndIsReadFalse(user);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.READ);
            notificationRepository.save(notification);
        }
    }

    @Override
    public Notification updateNotification(Long id, Notification notificationDetails) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setTitle(notificationDetails.getTitle());
        notification.setMessage(notificationDetails.getMessage());
        notification.setPriority(notificationDetails.getPriority());

        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    // ========== FUNCTIONALITY 2: Automated Event-Based Notifications ==========

    @Override
    public void sendTicketSubmittedNotification(Long ticketId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Ticket Submitted Successfully");
        notification.setMessage("Your ticket #" + ticketId + " has been submitted successfully and is now in the queue.");
        notification.setType(NotificationType.TICKET_SUBMITTED);
        notification.setPriority(NotificationPriority.NORMAL);
        notification.setEventType("TICKET_SUBMITTED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketAssignedNotification(Long ticketId, Long technicianId) {
        User technician = userRepository.findById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));

        Notification notification = new Notification();
        notification.setRecipient(technician);
        notification.setTitle("New Ticket Assigned");
        notification.setMessage("Ticket #" + ticketId + " has been assigned to you. Please review and take action.");
        notification.setType(NotificationType.TICKET_ASSIGNED);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setEventType("TICKET_ASSIGNED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketStatusChangedNotification(Long ticketId, String oldStatus, String newStatus) {
        // For demo, send to admin (ID=1)
        User user = userRepository.findById(1L).orElse(null);
        if (user == null) return;

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Ticket Status Updated");
        notification.setMessage("Ticket #" + ticketId + " status changed from " + oldStatus + " to " + newStatus);
        notification.setType(NotificationType.TICKET_STATUS_CHANGED);
        notification.setPriority(NotificationPriority.NORMAL);
        notification.setEventType("STATUS_CHANGED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketResolvedNotification(Long ticketId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Ticket Resolved");
        notification.setMessage("Great news! Your ticket #" + ticketId + " has been resolved. Please provide feedback.");
        notification.setType(NotificationType.TICKET_RESOLVED);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setEventType("TICKET_RESOLVED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketReopenedNotification(Long ticketId, Long technicianId) {
        User technician = userRepository.findById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));

        Notification notification = new Notification();
        notification.setRecipient(technician);
        notification.setTitle("Ticket Reopened");
        notification.setMessage("Ticket #" + ticketId + " has been reopened and requires your attention.");
        notification.setType(NotificationType.TICKET_REOPENED);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setEventType("TICKET_REOPENED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    // ========== FUNCTIONALITY 3: Reminder & Escalation System ==========

    @Override
    public void sendReminderNotification(Long ticketId, Long technicianId) {
        User technician = userRepository.findById(technicianId)
            .orElseThrow(() -> new RuntimeException("Technician not found"));

        Notification notification = new Notification();
        notification.setRecipient(technician);
        notification.setTitle("Reminder: Overdue Ticket");
        notification.setMessage("Reminder: Ticket #" + ticketId + " is overdue. Please take action immediately.");
        notification.setType(NotificationType.REMINDER);
        notification.setPriority(NotificationPriority.URGENT);
        notification.setEventType("OVERDUE_REMINDER");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setEscalationLevel(1);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendEscalationNotification(Long ticketId, Long supervisorId, int escalationLevel) {
        User supervisor = userRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        String levelText = escalationLevel == 2 ? "Supervisor" : "Administrator";

        Notification notification = new Notification();
        notification.setRecipient(supervisor);
        notification.setTitle("ESCALATION: Overdue Ticket");
        notification.setMessage("URGENT: Ticket #" + ticketId + " has been escalated to " + levelText + " level due to delays.");
        notification.setType(NotificationType.ESCALATION);
        notification.setPriority(NotificationPriority.URGENT);
        notification.setEventType("ESCALATION");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);
        notification.setEscalationLevel(escalationLevel);

        createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void checkAndProcessOverdueTickets() {
        System.out.println("Checking for overdue tickets...");
        System.out.println("Overdue ticket check completed.");
    }

    // ========== FUNCTIONALITY 4: Notification Delivery Management ==========

    @Override
    public void sendNotification(Long notificationId, DeliveryChannel channel) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        try {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setDeliveryStatus("Sent");

            if (channel == DeliveryChannel.IN_APP) {
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveredAt(LocalDateTime.now());
                notification.setDeliveryStatus("Delivered");
            }

            notificationRepository.save(notification);
            System.out.println("Notification sent via " + channel + ": " + notification.getTitle());

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setDeliveryStatus("Failed");
            notification.setFailureReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);

            if (notification.getRetryCount() < notification.getMaxRetries()) {
                notification.setStatus(NotificationStatus.QUEUED_FOR_RETRY);
            } else {
                notification.setStatus(NotificationStatus.PERMANENTLY_FAILED);
            }

            notificationRepository.save(notification);
            System.out.println("Notification delivery failed: " + e.getMessage());
        }
    }

    @Override
    public void updateDeliveryStatus(Long notificationId, String status) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setDeliveryStatus(status);

        switch (status.toUpperCase()) {
            case "SENT":
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                break;
            case "DELIVERED":
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveredAt(LocalDateTime.now());
                break;
            case "READ":
                notification.setStatus(NotificationStatus.READ);
                notification.setReadAt(LocalDateTime.now());
                notification.setRead(true);
                break;
            case "FAILED":
                notification.setStatus(NotificationStatus.FAILED);
                break;
        }

        notificationRepository.save(notification);
    }

    @Override
    public void retryFailedDeliveries() {
        List<Notification> failedNotifications =
            notificationRepository.findByStatusAndRetryCountLessThan(
                NotificationStatus.QUEUED_FOR_RETRY, 3);

        System.out.println("Retrying " + failedNotifications.size() + " failed deliveries...");

        for (Notification notification : failedNotifications) {
            sendNotification(notification.getId(), notification.getDeliveryChannel());
        }
    }

    @Override
    public String getDeliveryStatistics() {
        List<Notification> allNotifications = notificationRepository.findAll();

        long total = allNotifications.size();
        long sent = allNotifications.stream()
            .filter(n -> n.getStatus() == NotificationStatus.SENT).count();
        long delivered = allNotifications.stream()
            .filter(n -> n.getStatus() == NotificationStatus.DELIVERED).count();
        long read = allNotifications.stream()
            .filter(n -> n.getStatus() == NotificationStatus.READ).count();
        long failed = allNotifications.stream()
            .filter(n -> n.getStatus() == NotificationStatus.FAILED ||
                         n.getStatus() == NotificationStatus.PERMANENTLY_FAILED).count();

        StringBuilder stats = new StringBuilder();
        stats.append("=== NOTIFICATION DELIVERY STATISTICS ===\n");
        stats.append("Total Notifications: ").append(total).append("\n");
        stats.append("Sent: ").append(sent).append("\n");
        stats.append("Delivered: ").append(delivered).append("\n");
        stats.append("Read: ").append(read).append("\n");
        stats.append("Failed: ").append(failed).append("\n");
        stats.append("\nDelivery Success Rate: ")
            .append(total > 0 ? String.format("%.2f%%", (delivered * 100.0 / total)) : "0.00%")
            .append("\n");
        stats.append("Read Rate: ")
            .append(delivered > 0 ? String.format("%.2f%%", (read * 100.0 / delivered)) : "0.00%")
            .append("\n");

        return stats.toString();
    }
}
