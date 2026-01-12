package com.um.helpdesk.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ========== FUNCTIONALITY 1: Notification Management (CRUD) ==========

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        System.out.println("GET /api/notifications - Fetching all notifications");
        List<Notification> notifications = notificationService.getAllNotifications();
        System.out.println("✓ Found " + notifications.size() + " notification(s)\n");
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        System.out.println("GET /api/notifications/" + id + " - Fetching notification");
        Notification notification = notificationService.getNotificationById(id);
        System.out.println("✓ Notification found: " + notification.getTitle() + "\n");
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        System.out.println("GET /api/notifications/user/" + userId + " - Fetching user notifications");
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        System.out.println("✓ Found " + notifications.size() + " notification(s) for user\n");
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        System.out.println("GET /api/notifications/user/" + userId + "/unread - Fetching unread notifications");
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        System.out.println("✓ Found " + notifications.size() + " unread notification(s)\n");
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        System.out.println("GET /api/notifications/user/" + userId + "/unread-count");
        long count = notificationService.getUnreadCount(userId);
        System.out.println("✓ Unread count: " + count + "\n");
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        System.out.println("POST /api/notifications - Creating notification");
        Notification saved = notificationService.createNotification(notification);
        System.out.println("✓ Notification created: " + saved.getTitle() + " (ID: " + saved.getId() + ")\n");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable Long id,
            @RequestBody Notification notification) {
        System.out.println("PUT /api/notifications/" + id + " - Updating notification");
        Notification updated = notificationService.updateNotification(id, notification);
        System.out.println("✓ Notification updated: " + updated.getTitle() + "\n");
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        System.out.println("PUT /api/notifications/" + id + "/mark-read");
        notificationService.markAsRead(id);
        System.out.println("✓ Notification marked as read\n");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        System.out.println("PUT /api/notifications/user/" + userId + "/mark-all-read");
        notificationService.markAllAsRead(userId);
        System.out.println("✓ All notifications marked as read\n");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        System.out.println("DELETE /api/notifications/" + id);
        notificationService.deleteNotification(id);
        System.out.println("✓ Notification deleted\n");
        return ResponseEntity.noContent().build();
    }

    // ========== FUNCTIONALITY 2: Automated Event-Based Notifications ==========

    @PostMapping("/event/ticket-submitted")
    public ResponseEntity<Void> sendTicketSubmittedNotification(
            @RequestParam Long ticketId,
            @RequestParam Long userId) {
        System.out.println("POST /api/notifications/event/ticket-submitted");
        notificationService.sendTicketSubmittedNotification(ticketId, userId);
        System.out.println("✓ Ticket submitted notification sent\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/event/ticket-assigned")
    public ResponseEntity<Void> sendTicketAssignedNotification(
            @RequestParam Long ticketId,
            @RequestParam Long technicianId) {
        System.out.println("POST /api/notifications/event/ticket-assigned");
        notificationService.sendTicketAssignedNotification(ticketId, technicianId);
        System.out.println("✓ Ticket assigned notification sent\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/event/ticket-status-changed")
    public ResponseEntity<Void> sendTicketStatusChangedNotification(
            @RequestParam Long ticketId,
            @RequestParam String oldStatus,
            @RequestParam String newStatus) {
        System.out.println("POST /api/notifications/event/ticket-status-changed");
        notificationService.sendTicketStatusChangedNotification(ticketId, oldStatus, newStatus);
        System.out.println("✓ Ticket status changed notification sent\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/event/ticket-resolved")
    public ResponseEntity<Void> sendTicketResolvedNotification(
            @RequestParam Long ticketId,
            @RequestParam Long userId) {
        System.out.println("POST /api/notifications/event/ticket-resolved");
        notificationService.sendTicketResolvedNotification(ticketId, userId);
        System.out.println("✓ Ticket resolved notification sent\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/event/ticket-reopened")
    public ResponseEntity<Void> sendTicketReopenedNotification(
            @RequestParam Long ticketId,
            @RequestParam Long technicianId) {
        System.out.println("POST /api/notifications/event/ticket-reopened");
        notificationService.sendTicketReopenedNotification(ticketId, technicianId);
        System.out.println("✓ Ticket reopened notification sent\n");
        return ResponseEntity.ok().build();
    }

    // ========== FUNCTIONALITY 3: Reminder & Escalation System ==========

    @PostMapping("/reminder")
    public ResponseEntity<Void> sendReminderNotification(
            @RequestParam Long ticketId,
            @RequestParam Long technicianId) {
        System.out.println("POST /api/notifications/reminder");
        notificationService.sendReminderNotification(ticketId, technicianId);
        System.out.println("✓ Reminder notification sent\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/escalation")
    public ResponseEntity<Void> sendEscalationNotification(
            @RequestParam Long ticketId,
            @RequestParam Long supervisorId,
            @RequestParam int escalationLevel) {
        System.out.println("POST /api/notifications/escalation");
        notificationService.sendEscalationNotification(ticketId, supervisorId, escalationLevel);
        System.out.println("✓ Escalation notification sent\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-overdue")
    public ResponseEntity<Void> checkOverdueTickets() {
        System.out.println("POST /api/notifications/check-overdue");
        notificationService.checkAndProcessOverdueTickets();
        System.out.println("✓ Overdue tickets check completed\n");
        return ResponseEntity.ok().build();
    }

    // ========== FUNCTIONALITY 4: Notification Delivery Management ==========

    @PutMapping("/{id}/delivery-status")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        System.out.println("PUT /api/notifications/" + id + "/delivery-status");
        notificationService.updateDeliveryStatus(id, status);
        System.out.println("✓ Delivery status updated to: " + status + "\n");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<Void> retryFailedDeliveries() {
        System.out.println("POST /api/notifications/retry-failed");
        notificationService.retryFailedDeliveries();
        System.out.println("✓ Failed deliveries retry initiated\n");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<String> getDeliveryStatistics() {
        System.out.println("GET /api/notifications/statistics");
        String stats = notificationService.getDeliveryStatistics();
        System.out.println("✓ Statistics retrieved\n");
        return ResponseEntity.ok(stats);
    }
}
