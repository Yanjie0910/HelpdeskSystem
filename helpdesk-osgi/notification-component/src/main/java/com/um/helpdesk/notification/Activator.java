package com.um.helpdesk.notification;

import com.um.helpdesk.service.NotificationService;
import com.um.helpdesk.notification.impl.NotificationServiceImpl;
import com.um.helpdesk.entity.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.List;

public class Activator implements BundleActivator {

    private EntityManagerFactory emf;
    private EntityManager em;
    private ServiceRegistration<NotificationService> serviceRegistration;
    private boolean running = true;
    private Long currentUserId = 1L; // Default to admin for demo
    private boolean isAdmin = true;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println(">>> Notification Component: Starting...");

        new Thread(() -> {
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                System.out.println(">>> JPA(Notification): Initializing EntityManagerFactory...");
                emf = Persistence.createEntityManagerFactory("helpdesk-pu");
                em = emf.createEntityManager();
                System.out.println(">>> JPA(Notification): Initialized successfully.");

                NotificationServiceImpl serviceImpl = new NotificationServiceImpl(em);

                if (context != null) {
                    Dictionary<String, String> props = new Hashtable<>();
                    props.put("component", "notification");
                    serviceRegistration = context.registerService(NotificationService.class, serviceImpl, props);
                    System.out.println(">>> OSGi: NotificationService registered.");
                } else {
                    System.out.println(">>> Running in standalone mode (no OSGi context).");
                }

                runConsoleDemo(serviceImpl);

            } catch (Exception e) {
                System.err.println("!!! ERROR in Notification Activator Thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        System.out.println(">>> Notification Activator thread launched. Status will be ACTIVE soon.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        running = false;
        System.out.println("Stopping Notification Component...");
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private void runConsoleDemo(NotificationService service) {
        Scanner sc = new Scanner(System.in);
        boolean systemRunning = true;

        while (systemRunning) {
            // 1. Select User
            boolean userSelected = selectDemoUser(sc);
            if (!userSelected) {
                System.out.println("System terminated.");
                break;
            }

            boolean running = true;
            while (running) {
                // 2. Show Role Header + Menu
                displayRoleBasedMenu(); // Combined header + menu options
                System.out.print("Choose option: ");

                int choice = -1;
                try {
                    if (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (!line.trim().isEmpty()) {
                            choice = Integer.parseInt(line);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }

                if (choice == 0) {
                    running = false; // Exit Notification Menu -> Go to Session Ended
                } else {
                    handleMenuChoice(choice, sc, service);
                }
            }

            // 3. Session Ended Menu
            displaySessionEndedMenu();
            System.out.print("Choose option: ");

            int exitChoice = -1;
            try {
                if (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (!line.trim().isEmpty()) {
                        exitChoice = Integer.parseInt(line);
                    }
                }
            } catch (NumberFormatException e) {
            }

            if (exitChoice == 0) {
                systemRunning = false;
                System.out.println("\n👋 Thank you for using UM Helpdesk System. Goodbye!");
            }
            // else (Option 1 or others) -> loops back to Select User
        }
    }

    private boolean selectDemoUser(Scanner sc) {
        System.out.println("\n┌────────────────────────────────────────────────────────────┐");
        System.out.println("│                   SELECT DEMO USER                         │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. Admin - Dr. World                                      │");
        System.out.println("│  2. Student - Lily Tan                                     │");
        System.out.println("│  3. Staff - Muthu                                          │");
        System.out.println("│  4. Technician - Bob Lee                                   │");
        System.out.println("│  0. Exit                                                   │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.print("Choose user (1-4): ");

        int choice = -1;
        try {
            if (sc.hasNextLine()) {
                String line = sc.nextLine();
                choice = Integer.parseInt(line);
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        if (choice == 0)
            return false;

        switch (choice) {
            case 1 -> {
                currentUserId = 1L;
                isAdmin = true;
            }
            case 2, 3 -> {
                currentUserId = (long) choice;
                isAdmin = false;
            }
            case 4 -> {
                currentUserId = 4L;
                isAdmin = false;
            }
            default -> {
                currentUserId = 1L;
                isAdmin = true;
            }
        }
        return true;
    }

    private void displayRoleBasedMenu() {
        String roleName = isAdmin ? "ADMIN" : "USER";
        // Note: Ideally we'd map "USER" to specific roles but for this demo plain
        // "USER" or logic is fine.
        // Actually, let's make it look like the requested output "Role: ADMIN"

        String userName = switch (currentUserId.intValue()) {
            case 1 -> "Dr. World";
            case 2 -> "Lily Tan";
            case 3 -> "Muthu";
            case 4 -> "Bob Lee";
            default -> "User " + currentUserId;
        };

        String displayRole = isAdmin ? "ADMIN" : "USER"; // Simplification for OSGi demo

        System.out.println("\n┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  Logged in as: " + String.format("%-44s", userName) + " │");
        System.out.println("│  Role: " + String.format("%-52s", displayRole) + " │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│                   NOTIFICATION MENU                        │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. Notification Management (CRUD)                         │");
        System.out.println("│  2. Automated Event-Based Notifications                    │");
        System.out.println("│  3. Reminder & Escalation System                           │");
        System.out.println("│  4. Notification Delivery Management                       │");
        System.out.println("│  0.  Exit                                                  │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
    }

    private void displaySessionEndedMenu() {
        String userName = switch (currentUserId.intValue()) {
            case 1 -> "Dr. World";
            case 2 -> "Lily Tan";
            case 3 -> "Muthu";
            case 4 -> "Bob Lee";
            default -> "User " + currentUserId;
        };

        System.out.println("\n┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  Session ended for: " + String.format("%-39s", userName) + " │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. Switch to another user                                 │");
        System.out.println("│  0. Exit system completely                                 │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
    }

    private void handleMenuChoice(int choice, Scanner sc, NotificationService service) {
        try {
            switch (choice) {
                case 1 -> manageNotifications(sc, service);
                case 2 -> automatedNotifications(sc, service);
                case 3 -> reminderEscalation(sc, service);
                case 4 -> deliveryManagement(sc, service);
                default -> System.out.println("\n❌ Invalid option.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== FUNCTIONALITY 1: Notification Management (CRUD) ==========

    private void manageNotifications(Scanner sc, NotificationService service) {
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
                case 1 -> viewMyNotifications(service);
                case 2 -> viewUnreadNotifications(service);
                case 3 -> markAsRead(sc, service);
                case 4 -> markAllAsRead(service);
                case 5 -> deleteNotification(sc, service);
                case 6 -> {
                    if (isAdmin)
                        viewAllNotifications(service);
                }
                case 7 -> {
                    if (isAdmin)
                        createTestNotification(sc, service);
                }
                case 0 -> back = true;
                default -> System.out.println("\n❌ Invalid option.\n");
            }
        }
    }

    private void viewMyNotifications(NotificationService service) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("                  MY NOTIFICATIONS                        ");
        System.out.println("════════════════════════════════════════════════════════");

        var notifications = service.getNotificationsByUser(currentUserId);

        if (notifications.isEmpty()) {
            System.out.println("📭 No notifications found.\n");
            return;
        }

        long unreadCount = service.getUnreadCount(currentUserId);
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
                    n.getCreatedAt().toLocalDate().toString()));
        }
        System.out.println();
    }

    private void viewUnreadNotifications(NotificationService service) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("              UNREAD NOTIFICATIONS                        ");
        System.out.println("════════════════════════════════════════════════════════");

        var notifications = service.getUnreadNotifications(currentUserId);

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

    private void markAsRead(Scanner sc, NotificationService service) {
        var unreadNotifications = service.getUnreadNotifications(currentUserId);

        if (unreadNotifications.isEmpty()) {
            System.out.println("\n✅ No unread notifications.\n");
            return;
        }

        System.out.println("\n📝 Your Unread Notifications:");
        System.out.println("════════════════════════════════════════════════════════");
        for (var notif : unreadNotifications) {
            System.out.printf("ID: %d | %s | Priority: %s | Date: %s%n",
                    notif.getId(),
                    notif.getTitle(),
                    notif.getPriority(),
                    notif.getCreatedAt());
        }
        System.out.println("════════════════════════════════════════════════════════");

        System.out.print("\n📝 Enter Notification ID to mark as read: ");
        String input = sc.nextLine();

        try {
            Long id = Long.parseLong(input);
            service.markAsRead(id);
            System.out.println("✅ Notification marked as read!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Invalid notification ID. Please enter a numeric ID.\n");
        } catch (RuntimeException e) {
            System.out.println("❌ " + e.getMessage() + "\n");
        }
    }

    private void markAllAsRead(NotificationService service) {
        try {
            service.markAllAsRead(currentUserId);
            System.out.println("✅ All notifications marked as read!\n");
        } catch (RuntimeException e) {
            System.out.println("❌ " + e.getMessage() + "\n");
        }
    }

    private void deleteNotification(Scanner sc, NotificationService service) {
        var userNotifications = service.getNotificationsByUser(currentUserId);

        if (userNotifications.isEmpty()) {
            System.out.println("\n❌ No notifications available to delete.\n");
            return;
        }

        System.out.println("\n🗑️ Your Notifications:");
        System.out.println("════════════════════════════════════════════════════════");
        for (var notif : userNotifications) {
            System.out.printf("ID: %d | %s | Status: %s | Date: %s%n",
                    notif.getId(),
                    notif.getTitle(),
                    notif.getStatus(),
                    notif.getCreatedAt());
        }
        System.out.println("════════════════════════════════════════════════════════");

        System.out.print("\n🗑️ Enter Notification ID to delete: ");
        String input = sc.nextLine();

        try {
            Long id = Long.parseLong(input);

            System.out.print("⚠️ Are you sure? (yes/no): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("yes")) {
                try {
                    service.deleteNotification(id);
                    System.out.println("✅ Notification deleted!\n");
                } catch (RuntimeException e) {
                    System.out.println("❌ " + e.getMessage() + "\n");
                }
            } else {
                System.out.println("❌ Deletion cancelled.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Invalid notification ID. Please enter a numeric ID.\n");
        }
    }

    private void viewAllNotifications(NotificationService service) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("           ALL NOTIFICATIONS (ADMIN VIEW)                 ");
        System.out.println("════════════════════════════════════════════════════════");

        var notifications = service.getAllNotifications();

        if (notifications.isEmpty()) {
            System.out.println("📭 No notifications in system.\n");
            return;
        }

        System.out.println("📬 Total notifications: " + notifications.size() + "\n");

        System.out.println(String.format("%-5s %-20s %-30s %-15s %-10s",
                "ID", "Recipient", "Title", "Type", "Status"));
        System.out.println("─".repeat(90));

        for (Notification n : notifications) {
            String recipientName = (n.getRecipient() != null) ? n.getRecipient().getFullName() : "N/A";
            System.out.println(String.format("%-5d %-20s %-30s %-15s %-10s",
                    n.getId(),
                    truncate(recipientName, 18),
                    truncate(n.getTitle(), 28),
                    n.getType(),
                    n.getStatus()));
        }
        System.out.println();
    }

    private void createTestNotification(Scanner sc, NotificationService service) {
        System.out.println("\n--- CREATE TEST NOTIFICATION ---");
        System.out.println("Note: Simulating user creation. In real OSGi, fetch from User service.\n");

        System.out.print("📝 Recipient User ID (1-4): ");
        String userInput = sc.nextLine();

        System.out.print("Title: ");
        String title = sc.nextLine();

        System.out.print("Message: ");
        String message = sc.nextLine();

        System.out.println("Priority (1=LOW, 2=NORMAL, 3=HIGH, 4=URGENT): ");
        String priorityInput = sc.nextLine();

        try {
            Long userId = Long.parseLong(userInput);
            int priorityChoice = Integer.parseInt(priorityInput);

            NotificationPriority priority = switch (priorityChoice) {
                case 1 -> NotificationPriority.LOW;
                case 2 -> NotificationPriority.NORMAL;
                case 3 -> NotificationPriority.HIGH;
                case 4 -> NotificationPriority.URGENT;
                default -> NotificationPriority.NORMAL;
            };

            // Create custom notification
            // Note: In pure OSGi without user repo, we simulate by creating minimal user
            User user = em.find(User.class, userId);
            if (user == null) {
                System.out.println("❌ User not found with ID: " + userId + "\n");
                return;
            }

            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setPriority(priority);
            notification.setType(NotificationType.TICKET_SUBMITTED);
            notification.setEventType("CUSTOM_TEST");

            Notification created = service.createNotification(notification);
            service.sendNotification(created.getId(), DeliveryChannel.IN_APP);

            System.out.println("✅ Custom notification created and sent to user!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric values.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 2: Automated Event-Based Notifications ==========

    private void automatedNotifications(Scanner sc, NotificationService service) {
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
            case 1 -> simulateTicketSubmitted(sc, service);
            case 2 -> simulateTicketAssigned(sc, service);
            case 3 -> simulateTicketStatusChanged(sc, service);
            case 4 -> simulateTicketResolved(sc, service);
            case 5 -> simulateTicketReopened(sc, service);
        }
    }

    private void simulateTicketSubmitted(Scanner sc, NotificationService service) {
        System.out.print("\n📝 Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Enter User ID: ");
        String userInput = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            Long userId = Long.parseLong(userInput);
            service.sendTicketSubmittedNotification(ticketId, userId);
            System.out.println("✅ Ticket submitted notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric IDs.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketAssigned(Scanner sc, NotificationService service) {
        System.out.print("\n📝 Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Enter Technician ID: ");
        String techInput = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            Long techId = Long.parseLong(techInput);
            service.sendTicketAssignedNotification(ticketId, techId);
            System.out.println("✅ Ticket assigned notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric IDs.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketStatusChanged(Scanner sc, NotificationService service) {
        System.out.print("\n📝 Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Old Status: ");
        String oldStatus = sc.nextLine();
        System.out.print("New Status: ");
        String newStatus = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            service.sendTicketStatusChangedNotification(ticketId, oldStatus, newStatus);
            System.out.println("✅ Status changed notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter a valid numeric Ticket ID.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketResolved(Scanner sc, NotificationService service) {
        System.out.print("\n📝 Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Enter User ID: ");
        String userInput = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            Long userId = Long.parseLong(userInput);
            service.sendTicketResolvedNotification(ticketId, userId);
            System.out.println("✅ Ticket resolved notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric IDs.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void simulateTicketReopened(Scanner sc, NotificationService service) {
        System.out.print("\n📝 Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Enter Technician ID: ");
        String techInput = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            Long techId = Long.parseLong(techInput);
            service.sendTicketReopenedNotification(ticketId, techId);
            System.out.println("✅ Ticket reopened notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric IDs.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 3: Reminder & Escalation System ==========

    private void reminderEscalation(Scanner sc, NotificationService service) {
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
            case 1 -> sendReminder(sc, service);
            case 2 -> sendEscalation(sc, service);
            case 3 -> checkOverdueTickets(service);
        }
    }

    private void sendReminder(Scanner sc, NotificationService service) {
        System.out.print("\n⏰ Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Enter Technician ID: ");
        String techInput = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            Long techId = Long.parseLong(techInput);
            service.sendReminderNotification(ticketId, techId);
            System.out.println("✅ Reminder notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric IDs.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void sendEscalation(Scanner sc, NotificationService service) {
        System.out.print("\n⚠️ Enter Ticket ID: ");
        String ticketInput = sc.nextLine();
        System.out.print("Enter Supervisor ID: ");
        String supervisorInput = sc.nextLine();
        System.out.print("Escalation Level (1=Tech, 2=Supervisor, 3=Admin): ");
        String levelInput = sc.nextLine();

        try {
            Long ticketId = Long.parseLong(ticketInput);
            Long supervisorId = Long.parseLong(supervisorInput);
            int level = Integer.parseInt(levelInput);
            service.sendEscalationNotification(ticketId, supervisorId, level);
            System.out.println("✅ Escalation notification sent!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter valid numeric values.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void checkOverdueTickets(NotificationService service) {
        System.out.println("\n🔍 Checking for overdue tickets...");
        service.checkAndProcessOverdueTickets();
        System.out.println("✅ Overdue ticket check completed!\n");
    }

    // ========== FUNCTIONALITY 4: Notification Delivery Management ==========

    private void deliveryManagement(Scanner sc, NotificationService service) {
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
            case 1 -> updateDeliveryStatus(sc, service);
            case 2 -> retryFailed(service);
            case 3 -> viewStatistics(service);
        }
    }

    private void updateDeliveryStatus(Scanner sc, NotificationService service) {
        var allNotifications = service.getAllNotifications();

        if (allNotifications.isEmpty()) {
            System.out.println("\n❌ No notifications available.\n");
            return;
        }

        System.out.println("\n📋 Available Notifications:");
        System.out.println("════════════════════════════════════════════════════════");
        for (var notif : allNotifications) {
            String recipientName = (notif.getRecipient() != null) ? notif.getRecipient().getFullName() : "N/A";
            System.out.printf("ID: %d | %s | Status: %s | User: %s%n",
                    notif.getId(),
                    notif.getTitle(),
                    notif.getDeliveryStatus(),
                    recipientName);
        }
        System.out.println("════════════════════════════════════════════════════════");

        System.out.print("\n📋 Enter Notification ID: ");
        String input = sc.nextLine();

        try {
            Long id = Long.parseLong(input);

            System.out.print("Enter Status (SENT/DELIVERED/READ/FAILED): ");
            String status = sc.nextLine();

            service.updateDeliveryStatus(id, status);
            System.out.println("✅ Delivery status updated!\n");
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Invalid notification ID. Please enter a numeric ID.\n");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage() + "\n");
        }
    }

    private void retryFailed(NotificationService service) {
        System.out.println("\n🔄 Retrying failed deliveries...");
        service.retryFailedDeliveries();
        System.out.println("✅ Retry completed!\n");
    }

    private void viewStatistics(NotificationService service) {
        System.out.println("\n📊 Delivery Statistics:\n");
        String stats = service.getDeliveryStatistics();
        System.out.println(stats);
    }

    private String truncate(String str, int length) {
        if (str == null)
            return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}
