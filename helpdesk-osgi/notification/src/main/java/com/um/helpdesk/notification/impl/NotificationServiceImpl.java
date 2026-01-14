package com.um.helpdesk.notification.impl;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.notification.repository.NotificationRepository;
import com.um.helpdesk.service.NotificationService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationServiceImpl implements NotificationService {

    private final EntityManager em;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(EntityManager em) {
        this.em = em;
        this.notificationRepository = new NotificationRepository(em);
    }

    // ========== FUNCTIONALITY 1: Notification Management (CRUD) ==========

    @Override
    public Notification createNotification(Notification notification) {
        em.getTransaction().begin();
        Notification saved = notificationRepository.save(notification);
        em.getTransaction().commit();
        return saved;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public Notification getNotificationById(Long id) {
        Notification n = notificationRepository.findById(id);
        if (n == null) {
            throw new RuntimeException("Notification not found with id: " + id);
        }
        return n;
    }

    @Override
    public List<Notification> getNotificationsByUser(Long userId) {
        User user = findUserById(userId);
        return notificationRepository.findByRecipient(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = findUserById(userId);
        return notificationRepository.findUnreadByRecipient(user);
    }

    @Override
    public void markAsRead(Long notificationId) {
        em.getTransaction().begin();
        Notification notification = getNotificationById(notificationId);
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
        em.getTransaction().commit();
    }

    @Override
    public void markAllAsRead(Long userId) {
        em.getTransaction().begin();
        User user = findUserById(userId);
        List<Notification> unread = notificationRepository.findUnreadByRecipient(user);
        for (Notification n : unread) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            n.setStatus(NotificationStatus.READ);
            notificationRepository.save(n);
        }
        em.getTransaction().commit();
    }

    @Override
    public Notification updateNotification(Long id, Notification notificationDetails) {
        em.getTransaction().begin();
        Notification n = getNotificationById(id);
        n.setTitle(notificationDetails.getTitle());
        n.setMessage(notificationDetails.getMessage());
        n.setPriority(notificationDetails.getPriority());
        Notification updated = notificationRepository.save(n);
        em.getTransaction().commit();
        return updated;
    }

    @Override
    public void deleteNotification(Long id) {
        em.getTransaction().begin();
        Notification n = notificationRepository.findById(id);
        if (n == null) {
            em.getTransaction().rollback();
            throw new RuntimeException("Notification not found with id: " + id);
        }
        notificationRepository.delete(n);
        em.getTransaction().commit();
    }

    @Override
    public long getUnreadCount(Long userId) {
        User user = findUserById(userId);
        return notificationRepository.countUnreadByRecipient(user);
    }

    // ========== FUNCTIONALITY 2: Automated Event-Based Notifications ==========

    @Override
    public void sendTicketSubmittedNotification(Long ticketId, Long userId) {
        User user = findUserById(userId);

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Ticket Submitted Successfully");
        notification.setMessage("Your ticket #" + ticketId + " has been submitted successfully and is now in the queue.");
        notification.setType(NotificationType.TICKET_SUBMITTED);
        notification.setPriority(NotificationPriority.NORMAL);
        notification.setEventType("TICKET_SUBMITTED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketAssignedNotification(Long ticketId, Long technicianId) {
        User technician = findUserById(technicianId);

        Notification notification = new Notification();
        notification.setRecipient(technician);
        notification.setTitle("New Ticket Assigned");
        notification.setMessage("Ticket #" + ticketId + " has been assigned to you. Please review and take action.");
        notification.setType(NotificationType.TICKET_ASSIGNED);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setEventType("TICKET_ASSIGNED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketStatusChangedNotification(Long ticketId, String oldStatus, String newStatus) {
        // For demo: send to first Administrator we can find
        User admin = findAnyAdministrator();
        if (admin == null) {
            System.out.println("No administrator found for status change notification.");
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(admin);
        notification.setTitle("Ticket Status Updated");
        notification.setMessage("Ticket #" + ticketId + " status changed from " + oldStatus + " to " + newStatus);
        notification.setType(NotificationType.TICKET_STATUS_CHANGED);
        notification.setPriority(NotificationPriority.NORMAL);
        notification.setEventType("STATUS_CHANGED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketResolvedNotification(Long ticketId, Long userId) {
        User user = findUserById(userId);

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Ticket Resolved");
        notification.setMessage("Great news! Your ticket #" + ticketId + " has been resolved. Please provide feedback.");
        notification.setType(NotificationType.TICKET_RESOLVED);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setEventType("TICKET_RESOLVED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendTicketReopenedNotification(Long ticketId, Long technicianId) {
        User technician = findUserById(technicianId);

        Notification notification = new Notification();
        notification.setRecipient(technician);
        notification.setTitle("Ticket Reopened");
        notification.setMessage("Ticket #" + ticketId + " has been reopened and requires your attention.");
        notification.setType(NotificationType.TICKET_REOPENED);
        notification.setPriority(NotificationPriority.HIGH);
        notification.setEventType("TICKET_REOPENED");
        notification.setRelatedTicketId(ticketId);
        notification.setDeliveryChannel(DeliveryChannel.IN_APP);

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    // ========== FUNCTIONALITY 3: Reminder & Escalation System ==========

    @Override
    public void sendReminderNotification(Long ticketId, Long technicianId) {
        User technician = findUserById(technicianId);

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

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void sendEscalationNotification(Long ticketId, Long supervisorId, int escalationLevel) {
        User supervisor = findUserById(supervisorId);

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

        notification = createNotification(notification);
        sendNotification(notification.getId(), DeliveryChannel.IN_APP);
    }

    @Override
    public void checkAndProcessOverdueTickets() {
        // For now just log like Spring Boot demo
        System.out.println("NotificationService: Checking for overdue tickets (demo only)...");
    }

    // ========== FUNCTIONALITY 4: Notification Delivery Management ==========

    @Override
    public void sendNotification(Long notificationId, DeliveryChannel channel) {
        em.getTransaction().begin();
        Notification notification = getNotificationById(notificationId);

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
            em.getTransaction().commit();

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
            em.getTransaction().commit();

            System.out.println("Notification delivery failed: " + e.getMessage());
        }
    }

    @Override
    public void updateDeliveryStatus(Long notificationId, String status) {
        em.getTransaction().begin();
        Notification notification = getNotificationById(notificationId);

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
            default:
                break;
        }

        notificationRepository.save(notification);
        em.getTransaction().commit();
    }

    @Override
    public void retryFailedDeliveries() {
        List<Notification> failed = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.QUEUED_FOR_RETRY, 3);

        System.out.println("Retrying " + failed.size() + " failed notification deliveries...");
        for (Notification n : failed) {
            sendNotification(n.getId(), n.getDeliveryChannel());
        }
    }

    @Override
    public String getDeliveryStatistics() {
        List<Notification> all = notificationRepository.findAll();

        long total = all.size();
        long sent = all.stream().filter(n -> n.getStatus() == NotificationStatus.SENT).count();
        long delivered = all.stream().filter(n -> n.getStatus() == NotificationStatus.DELIVERED).count();
        long read = all.stream().filter(n -> n.getStatus() == NotificationStatus.READ).count();
        long failed = all.stream().filter(n ->
                n.getStatus() == NotificationStatus.FAILED ||
                        n.getStatus() == NotificationStatus.PERMANENTLY_FAILED).count();

        StringBuilder sb = new StringBuilder();
        sb.append("=== NOTIFICATION DELIVERY STATISTICS (OSGi) ===\n");
        sb.append("Total Notifications: ").append(total).append("\n");
        sb.append("Sent: ").append(sent).append("\n");
        sb.append("Delivered: ").append(delivered).append("\n");
        sb.append("Read: ").append(read).append("\n");
        sb.append("Failed: ").append(failed).append("\n");
        sb.append("\nDelivery Success Rate: ")
                .append(total > 0 ? String.format("%.2f%%", (delivered * 100.0 / total)) : "0.00%")
                .append("\n");
        sb.append("Read Rate: ")
                .append(delivered > 0 ? String.format("%.2f%%", (read * 100.0 / delivered)) : "0.00%")
                .append("\n");

        return sb.toString();
    }

    // ===== Helper methods =====

    private User findUserById(Long id) {
        User u = em.find(User.class, id);
        if (u == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return u;
    }

    private User findAnyAdministrator() {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role",
                User.class);
        q.setParameter("role", UserRole.ADMIN);
        List<User> admins = q.setMaxResults(1).getResultList();
        return admins.isEmpty() ? null : admins.get(0);
    }
}

