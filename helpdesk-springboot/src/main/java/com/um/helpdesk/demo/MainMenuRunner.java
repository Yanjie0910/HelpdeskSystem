package com.um.helpdesk.demo;

import java.util.Scanner;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;

@Component
@Order(1)
public class MainMenuRunner implements CommandLineRunner {

    private final UserService userService;
    private final UserManagementConsoleRunner userModule;
    private final NotificationConsoleRunner notificationModule;
    private final TicketAssignmentConsoleRunner ticketAssignmentModule;
    private final ReportingConsoleRunner reportingRunner;

    private User currentUser = null;

    public MainMenuRunner(
            UserService userService,
            UserManagementConsoleRunner userModule,
            NotificationConsoleRunner notificationModule,
            TicketAssignmentConsoleRunner ticketAssignmentModule,
            ReportingConsoleRunner reportingRunner
    ) {
        this.userService = userService;
        this.userModule = userModule;
        this.notificationModule = notificationModule;
        this.ticketAssignmentModule = ticketAssignmentModule;
        this.reportingRunner = reportingRunner;
    }

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║               UNIVERSITY MALAYA HELPDESK SYSTEM            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        boolean systemRunning = true;

        while (systemRunning) {
            if (!selectDemoUser(sc)) {
                System.out.println("User selection failed. System terminated.");
                sc.close();
                return;
            }

            boolean running = true;

            while (running) {
                displayRoleBasedMenu();
                System.out.print("Choose option: ");

                int choice = 0;
                try {
                    choice = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    continue;
                }

                switch (currentUser.getRole()) {
                    case ADMIN -> running = handleAdminMenu(choice, sc);
                    case STUDENT, STAFF -> running = handleStudentStaffMenu(choice, sc);
                    case TECHNICIAN -> running = handleTechnicianMenu(choice, sc);
                }
            }

            System.out.println("\n┌────────────────────────────────────────────────────────────┐");
            System.out.println("│  Session ended for: " + String.format("%-39s", currentUser.getFullName()) + " │");
            System.out.println("├────────────────────────────────────────────────────────────┤");
            System.out.println("│  1. Switch to another user                                 │");
            System.out.println("│  0. Exit system completely                                 │");
            System.out.println("└────────────────────────────────────────────────────────────┘");
            System.out.print("Choose option: ");

            int exitChoice = 0;
            try {
                exitChoice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {}

            if (exitChoice == 0) {
                systemRunning = false;
                System.out.println("\n👋 Thank you for using UM Helpdesk System. Goodbye!");
            }
        }
        sc.close();
    }

    private boolean selectDemoUser(Scanner sc) {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
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
            choice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {}

        try {
            currentUser = switch (choice) {
                case 1 -> userService.getUserById(1L);
                case 2 -> userService.getUserById(2L);
                case 3 -> userService.getUserById(3L);
                case 4 -> userService.getUserById(4L);
                default -> null;
            };

            if (currentUser == null) {
                System.out.println("\nInvalid selection.\n");
                return false;
            }
            return true;

        } catch (RuntimeException e) {
            System.out.println("\n Error: " + e.getMessage());
            System.out.println("TIP: Make sure test data is initialized.\n");
            return false;
        }
    }

    private void displayRoleBasedMenu() {
        System.out.println("\n┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  Logged in as: " + String.format("%-44s", currentUser.getFullName()) + " │");
        System.out.println("│  Role: " + String.format("%-52s", currentUser.getRole()) + " │");
        System.out.println("├────────────────────────────────────────────────────────────┤");

        switch (currentUser.getRole()) {
            case ADMIN -> displayAdminMenu();
            case STUDENT, STAFF -> displayStudentStaffMenu();
            case TECHNICIAN -> displayTechnicianMenu();
        }

        System.out.println("│  0.  Exit                                                │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
    }

    private void displayAdminMenu() {
        System.out.println("│                   ADMIN MENU                               │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. User Management Module                                 │");
        System.out.println("│  2. Reporting Module (View Stats/Generate Reports)         │");
        System.out.println("│  3. View All Tickets (Admin View)                          │");
        System.out.println("│  4. Notification Module                                    │");
    }

    private boolean handleAdminMenu(int choice, Scanner sc) {
        switch (choice) {
            case 1 -> userModule.runUserManagement(sc, currentUser);
            case 2 -> reportingRunner.runReportingDemo(); // LINKED HERE
            case 3 -> System.out.println("\n[Info] View All Tickets feature coming soon.\n");
            case 4 -> notificationModule.runNotificationManagement(sc, currentUser);
            case 0 -> { return false; }
            default -> System.out.println("\nInvalid option.\n");
        }
        return true;
    }

    private void displayStudentStaffMenu() {
        System.out.println("│                  STUDENT/STAFF MENU                        │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. Lodge New Ticket                                       │");
        System.out.println("│  2. View My Tickets                                        │");
        System.out.println("│  3. My Profile                                             │");
        System.out.println("│  4. My Notifications                                       │");
    }

    private boolean handleStudentStaffMenu(int choice, Scanner sc) {
        switch (choice) {
            case 1 -> System.out.println("\n[Info] Lodge Ticket feature coming soon.\n");
            case 2 -> System.out.println("\n[Info] My Tickets feature coming soon.\n");
            case 3 -> viewMyProfile();
            case 4 -> notificationModule.runNotificationManagement(sc, currentUser);
            case 0 -> { return false; }
            default -> System.out.println("\n Invalid option.\n");
        }
        return true;
    }

    private void displayTechnicianMenu() {
        System.out.println("│                 TECHNICIAN MENU                            │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. View All Tickets                                       │");
        System.out.println("│  2. View My Assigned Tickets                               │");
        System.out.println("│  3. Claim New Ticket (Self-Assignment)                     │");
        System.out.println("│  4. Reassign Ticket (Internal Re-assignment)               │");
        System.out.println("│  5. Transfer to Other Department                           │");
        System.out.println("│  6. View Assignment History                                │");
        System.out.println("│  7. Auto-Route Ticket                                      │");
        System.out.println("│  8. My Profile                                             │");
        System.out.println("│  9. My Notifications                                       │");
    }

    private boolean handleTechnicianMenu(int choice, Scanner sc) {
        switch (choice) {
            case 1 -> ticketAssignmentModule.viewAllTickets(sc, currentUser);
            case 2 -> ticketAssignmentModule.viewAssignedTickets(sc, currentUser);
            case 3 -> ticketAssignmentModule.claimTicket(sc, currentUser);
            case 4 -> ticketAssignmentModule.reassignTicket(sc, currentUser);
            case 5 -> ticketAssignmentModule.transferTicket(sc, currentUser);
            case 6 -> ticketAssignmentModule.viewAssignmentHistory(sc, currentUser);
            case 7 -> ticketAssignmentModule.autoRouteTicket(sc, currentUser);
            case 8 -> viewMyProfile();
            case 9 -> notificationModule.runNotificationManagement(sc, currentUser);
            case 0 -> { return false; }
            default -> System.out.println("\nInvalid option.\n");
        }
        return true;
    }

    private void viewMyProfile() {
        System.out.println("\n═════════════════════════════════════════════════════════");
        System.out.println("                      MY PROFILE                           ");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Name:  " + currentUser.getFullName());
        System.out.println("Role:  " + currentUser.getRole());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println();
    }
}