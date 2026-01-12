package com.um.helpdesk.demo;

import java.util.Scanner;
import org.springframework.stereotype.Component;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.NotificationService;

@Component
public class NotificationConsoleRunner {

    private final NotificationService notificationService;

    public NotificationConsoleRunner(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void runNotificationManagement(Scanner sc, User currentUser) {

        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("              NOTIFICATION MODULE                           ");
        System.out.println("══════════════════════════════════════════════════════════");

        boolean back = false;

        while (!back) {
            displayNotificationMenu(isAdmin);
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> manageNotifications(sc, currentUser, isAdmin);
                case 2 -> automatedNotifications(sc);
                case 3 -> reminderEscalation(sc);
                case 4 -> deliveryManagement(sc);
                case 0 -> back = true;
                default -> System.out.println("\n❌ Invalid option.\n");
            }
        }
    }

    private void displayNotificationMenu(boolean isAdmin) {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("              NOTIFICATION MENU                              ");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  1. Notification Management (CRUD)");
        System.out.println("  2. Automated Event-Based Notifications");
        System.out.println("  3. Reminder & Escalation System");
        System.out.println("  4. Notification Delivery Management");
        System.out.println("  0. Back to Main Menu");
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    // ========== FUNCTIONALITY 1: Notification Management (CRUD) ==========

    private void manageNotifications(Scanner sc, User currentUser, boolean isAdmin) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("     FUNCTIONALITY 1: NOTIFICATION MANAGEMENT (CRUD)      ");
        System.out.println("════════════════════════════════════════════════════════");

        boolean back = false;
        while (!back) {
            System.out.println("\n1. View My Notifications");
            System.out.println("2. View Unread Notifications");
            System.out.println("3. Mark Notification as Read");
            System.out.println("4. Mark All as Read");
            System.out.println("5. Delete Notification");
            if (isAdmin) {
                System.out.println("6. View All Notifications (Admin)");
                System.out.println("7. Create Test Notification (Admin)");
            }
            System.out.println("0. Back");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewMyNotifications(currentUser);
                case 2 -> viewUnreadNotifications(currentUser);
                case 3 -> markAsRead(sc, currentUser);
                case 4 -> markAllAsRead(currentUser);
                case 5 -> deleteNotification(sc, currentUser);
                case 6 -> { if (isAdmin) viewAllNotifications(); }
                case 7 -> { if (isAdmin) createTestNotification(sc, currentUser); }
                case 0 -> back = true;
                default -> System.out.println("\n❌ Invalid option.\n");
            }
        }
    }

    private void viewMyNotifications(User user) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("                  MY NOTIFICATIONS                        ");
        System.out.println("════════════════════════════════════════════════════════");

        var notifications = notificationService.getNotificationsByUser(user.getId());

        if (notifications.isEmpty()) {
            System.out.println("📭 No notifications found.\n");
            return;
        }

        long unreadCount = notificationService.getUnreadCount(user.getId());
        System.out.println("📬 Total: " + notifications.size() + " | Unread: " + unreadCount + "\n");

        System.out.println(String.format("%-5s %-10s %-30s %-15s %-12s",
            "ID", "Status", "Title", "Type", "Created"));
        System.out.println("─".repeat(80));

        for (Notification n : notifications) {
            String statusIcon = n.isRead() ? "✓" : "●";
            System.out.println(String.format("%-5d %-10s %-30s %-15s %-12s",
                n.getId(),
                statusIcon + " " + n.getStatus(),
                truncate(n.getTitle(), 28),
                n.getType(),
                n.getCreatedAt().toLocalDate().toString()
            ));
        }
        System.out.println();
    }

    private void viewUnreadNotifications(User user) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("              UNREAD NOTIFICATIONS                        ");
        System.out.println("════════════════════════════════════════════════════════");

        var notifications = notificationService.getUnreadNotifications(user.getId());

        if (notifications.isEmpty()) {
            System.out.println("✅ No unread notifications!\n");
            return;
        }

        System.out.println("📬 Unread notifications: " + notifications.size() + "\n");

        for (Notification n : notifications) {
            System.out.println("┌─────────────────────────────────────────────────────");
            System.out.println("│ ID: " + n.getId() + " | Type: " + n.getType() + " | Priority: " + n.getPriority());
            System.out.println("│ Title: " + n.getTitle());
            System.out.println("│ Message: " + n.getMessage());
            System.out.println("│ Created: " + n.getCreatedAt());
            System.out.println("└─────────────────────────────────────────────────────");
        }
        System.out.println();
    }

    private void markAsRead(Scanner sc, User user) {
        System.out.print("\n📝 Enter Notification ID to mark as read: ");
        Long id = sc.nextLong();
        sc.nextLine();

        try {
            notificationService.markAsRead(id);
            System.out.println("✅ Notification marked as read!\n");
        } catch (RuntimeException e) {
            System.out.println("❌ " + e.getMessage() + "\n");
        }
    }

    private void markAllAsRead(User user) {
        try {
            notificationService.markAllAsRead(user.getId());
            System.out.println("✅ All notifications marked as read!\n");
        } catch (RuntimeException e) {
            System.out.println("❌ " + e.getMessage() + "\n");
        }
    }

    private void deleteNotification(Scanner sc, User user) {
        System.out.print("\n🗑️ Enter Notification ID to delete: ");
        Long id = sc.nextLong();
        sc.nextLine();

        System.out.print("⚠️ Are you sure? (yes/no): ");
        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            try {
                notificationService.deleteNotification(id);
                System.out.println("✅ Notification deleted!\n");
            } catch (RuntimeException e) {
                System.out.println("❌ " + e.getMessage() + "\n");
            }
        } else {
            System.out.println("❌ Deletion cancelled.\n");
        }
    }

    private void viewAllNotifications() {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("           ALL NOTIFICATIONS (ADMIN VIEW)                 ");
        System.out.println("════════════════════════════════════════════════════════");

        var notifications = notificationService.getAllNotifications();

        if (notifications.isEmpty()) {
            System.out.println("📭 No notifications in system.\n");
            return;
        }

        System.out.println("📬 Total notifications: " + notifications.size() + "\n");

        System.out.println(String.format("%-5s %-20s %-30s %-15s %-10s",
            "ID", "Recipient", "Title", "Type", "Status"));
        System.out.println("─".repeat(90));

        for (Notification n : notifications) {
            System.out.println(String.format("%-5d %-20s %-30s %-15s %-10s",
                n.getId(),
                truncate(n.getRecipient().getFullName(), 18),
                truncate(n.getTitle(), 28),
                n.getType(),
                n.getStatus()
            ));
        }
        System.out.println();
    }

    private void createTestNotification(Scanner sc, User currentUser) {
        System.out.println("\n--- CREATE TEST NOTIFICATION ---");

        System.out.print("Recipient User ID: ");
        Long userId = sc.nextLong();
        sc.nextLine();

        System.out.print("Title: ");
        String title = sc.nextLine();

        System.out.print("Message: ");
        String message = sc.nextLine();

        System.out.println("Priority (1=LOW, 2=NORMAL, 3=HIGH, 4=URGENT): ");
        int priorityChoice = sc.nextInt();
        sc.nextLine();

        NotificationPriority priority = switch (priorityChoice) {
            case 1 -> NotificationPriority.LOW;
            case 2 -> NotificationPriority.NORMAL;
            case 3 -> NotificationPriority.HIGH;
            case 4 -> NotificationPriority.URGENT;
            default -> NotificationPriority.NORMAL;
        };

        try {
            notificationService.sendTicketSubmittedNotification(999L, userId);
            System.out.println("✅ Test notification created and sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 2: Automated Event-Based Notifications ==========

    private void automatedNotifications(Scanner sc) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("   FUNCTIONALITY 2: AUTOMATED EVENT-BASED NOTIFICATIONS  ");
        System.out.println("════════════════════════════════════════════════════════");

        System.out.println("\n1. Simulate Ticket Submitted Notification");
        System.out.println("2. Simulate Ticket Assigned Notification");
        System.out.println("3. Simulate Ticket Status Changed Notification");
        System.out.println("4. Simulate Ticket Resolved Notification");
        System.out.println("5. Simulate Ticket Reopened Notification");
        System.out.println("0. Back");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> simulateTicketSubmitted(sc);
            case 2 -> simulateTicketAssigned(sc);
            case 3 -> simulateTicketStatusChanged(sc);
            case 4 -> simulateTicketResolved(sc);
            case 5 -> simulateTicketReopened(sc);
        }
    }

    private void simulateTicketSubmitted(Scanner sc) {
        System.out.print("\n📝 Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter User ID: ");
        Long userId = sc.nextLong();
        sc.nextLine();

        try {
            notificationService.sendTicketSubmittedNotification(ticketId, userId);
            System.out.println("✅ Ticket submitted notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketAssigned(Scanner sc) {
        System.out.print("\n📝 Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter Technician ID: ");
        Long techId = sc.nextLong();
        sc.nextLine();

        try {
            notificationService.sendTicketAssignedNotification(ticketId, techId);
            System.out.println("✅ Ticket assigned notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketStatusChanged(Scanner sc) {
        System.out.print("\n📝 Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        sc.nextLine();
        System.out.print("Old Status: ");
        String oldStatus = sc.nextLine();
        System.out.print("New Status: ");
        String newStatus = sc.nextLine();

        try {
            notificationService.sendTicketStatusChangedNotification(ticketId, oldStatus, newStatus);
            System.out.println("✅ Status changed notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketResolved(Scanner sc) {
        System.out.print("\n📝 Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter User ID: ");
        Long userId = sc.nextLong();
        sc.nextLine();

        try {
            notificationService.sendTicketResolvedNotification(ticketId, userId);
            System.out.println("✅ Ticket resolved notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketReopened(Scanner sc) {
        System.out.print("\n📝 Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter Technician ID: ");
        Long techId = sc.nextLong();
        sc.nextLine();

        try {
            notificationService.sendTicketReopenedNotification(ticketId, techId);
            System.out.println("✅ Ticket reopened notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 3: Reminder & Escalation System ==========

    private void reminderEscalation(Scanner sc) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("     FUNCTIONALITY 3: REMINDER & ESCALATION SYSTEM       ");
        System.out.println("════════════════════════════════════════════════════════");

        System.out.println("\n1. Send Reminder Notification");
        System.out.println("2. Send Escalation Notification");
        System.out.println("3. Check and Process Overdue Tickets");
        System.out.println("0. Back");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> sendReminder(sc);
            case 2 -> sendEscalation(sc);
            case 3 -> checkOverdueTickets();
        }
    }

    private void sendReminder(Scanner sc) {
        System.out.print("\n⏰ Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter Technician ID: ");
        Long techId = sc.nextLong();
        sc.nextLine();

        try {
            notificationService.sendReminderNotification(ticketId, techId);
            System.out.println("✅ Reminder notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void sendEscalation(Scanner sc) {
        System.out.print("\n⚠️ Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter Supervisor ID: ");
        Long supervisorId = sc.nextLong();
        System.out.print("Escalation Level (1=Tech, 2=Supervisor, 3=Admin): ");
        int level = sc.nextInt();
        sc.nextLine();

        try {
            notificationService.sendEscalationNotification(ticketId, supervisorId, level);
            System.out.println("✅ Escalation notification sent!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void checkOverdueTickets() {
        System.out.println("\n🔍 Checking for overdue tickets...");
        notificationService.checkAndProcessOverdueTickets();
        System.out.println("✅ Overdue ticket check completed!\n");
    }

    // ========== FUNCTIONALITY 4: Notification Delivery Management ==========

    private void deliveryManagement(Scanner sc) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("   FUNCTIONALITY 4: NOTIFICATION DELIVERY MANAGEMENT     ");
        System.out.println("════════════════════════════════════════════════════════");

        System.out.println("\n1. Update Delivery Status");
        System.out.println("2. Retry Failed Deliveries");
        System.out.println("3. View Delivery Statistics");
        System.out.println("0. Back");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> updateDeliveryStatus(sc);
            case 2 -> retryFailed();
            case 3 -> viewStatistics();
        }
    }

    private void updateDeliveryStatus(Scanner sc) {
        System.out.print("\n📋 Enter Notification ID: ");
        Long id = sc.nextLong();
        sc.nextLine();

        System.out.print("Enter Status (SENT/DELIVERED/READ/FAILED): ");
        String status = sc.nextLine();

        try {
            notificationService.updateDeliveryStatus(id, status);
            System.out.println("✅ Delivery status updated!\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void retryFailed() {
        System.out.println("\n🔄 Retrying failed deliveries...");
        notificationService.retryFailedDeliveries();
        System.out.println("✅ Retry completed!\n");
    }

    private void viewStatistics() {
        System.out.println("\n📊 Delivery Statistics:\n");
        String stats = notificationService.getDeliveryStatistics();
        System.out.println(stats);
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}
