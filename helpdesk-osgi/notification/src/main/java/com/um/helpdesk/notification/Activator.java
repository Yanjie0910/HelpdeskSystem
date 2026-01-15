package com.um.helpdesk.notification;

import com.um.helpdesk.service.NotificationService;
import com.um.helpdesk.notification.impl.NotificationServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Activator implements BundleActivator {

    private EntityManagerFactory emf;
    private EntityManager em;
    private ServiceRegistration<NotificationService> serviceRegistration;
    private boolean running = true;

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

                Dictionary<String, String> props = new Hashtable<>();
                props.put("component", "notification");
                serviceRegistration = context.registerService(NotificationService.class, serviceImpl, props);
                System.out.println(">>> OSGi: NotificationService registered.");

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
        while (running) {
            displayMenu();
            System.out.print("Choose option: ");
            try {
                if (sc.hasNextInt()) {
                    int choice = sc.nextInt();
                    sc.nextLine();
                    if (choice == 0) {
                        break;
                    }
                    handleMenu(choice, service, sc);
                } else {
                    sc.nextLine();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n--- NOTIFICATION MODULE MENU ---");
        System.out.println("1. View all notifications");
        System.out.println("2. View delivery statistics");
        System.out.println("3. Send Ticket Submitted Notification (Demo)");
        System.out.println("4. Send Ticket Assigned Notification (Demo)");
        System.out.println("5. Send Ticket Resolved Notification (Demo)");
        System.out.println("6. Send Reminder Notification (Demo)");
        System.out.println("7. Mark notification as read");
        System.out.println("0. Exit Notification Demo");
    }

    private void handleMenu(int choice, NotificationService service, Scanner sc) {
        switch (choice) {
            case 1 -> viewAllNotifications(service);
            case 2 -> showStats(service);
            case 3 -> sendTicketSubmittedDemo(service, sc);
            case 4 -> sendTicketAssignedDemo(service, sc);
            case 5 -> sendTicketResolvedDemo(service, sc);
            case 6 -> sendReminderDemo(service, sc);
            case 7 -> markAsReadDemo(service, sc);
            default -> System.out.println("Invalid choice");
        }
    }

    private void viewAllNotifications(NotificationService service) {
        var list = service.getAllNotifications();
        if (list.isEmpty()) {
            System.out.println("No notifications found.");
            return;
        }
        list.forEach(n -> System.out.println(
                "#" + n.getId() + " [" + n.getType() + "] " + n.getTitle() +
                        " => status=" + n.getStatus() + ", recipientId=" +
                        (n.getRecipient() != null ? n.getRecipient().getId() : null)
        ));
    }

    private void showStats(NotificationService service) {
        String stats = service.getDeliveryStatistics();
        System.out.println(stats);
    }

    private void sendTicketSubmittedDemo(NotificationService service, Scanner sc) {
        try {
            System.out.print("Enter User ID (recipient): ");
            Long userId = sc.nextLong();
            sc.nextLine();
            System.out.print("Enter Ticket ID: ");
            Long ticketId = sc.nextLong();
            sc.nextLine();
            
            service.sendTicketSubmittedNotification(ticketId, userId);
            System.out.println("✓ Ticket Submitted notification sent successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void sendTicketAssignedDemo(NotificationService service, Scanner sc) {
        try {
            System.out.print("Enter Technician ID (recipient): ");
            Long technicianId = sc.nextLong();
            sc.nextLine();
            System.out.print("Enter Ticket ID: ");
            Long ticketId = sc.nextLong();
            sc.nextLine();
            
            service.sendTicketAssignedNotification(ticketId, technicianId);
            System.out.println("✓ Ticket Assigned notification sent successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void sendTicketResolvedDemo(NotificationService service, Scanner sc) {
        try {
            System.out.print("Enter User ID (recipient): ");
            Long userId = sc.nextLong();
            sc.nextLine();
            System.out.print("Enter Ticket ID: ");
            Long ticketId = sc.nextLong();
            sc.nextLine();
            
            service.sendTicketResolvedNotification(ticketId, userId);
            System.out.println("✓ Ticket Resolved notification sent successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void sendReminderDemo(NotificationService service, Scanner sc) {
        try {
            System.out.print("Enter Technician ID (recipient): ");
            Long technicianId = sc.nextLong();
            sc.nextLine();
            System.out.print("Enter Ticket ID: ");
            Long ticketId = sc.nextLong();
            sc.nextLine();
            
            service.sendReminderNotification(ticketId, technicianId);
            System.out.println("✓ Reminder notification sent successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void markAsReadDemo(NotificationService service, Scanner sc) {
        try {
            System.out.print("Enter Notification ID to mark as read: ");
            Long notificationId = sc.nextLong();
            sc.nextLine();
            
            service.markAsRead(notificationId);
            System.out.println("✓ Notification marked as read!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

