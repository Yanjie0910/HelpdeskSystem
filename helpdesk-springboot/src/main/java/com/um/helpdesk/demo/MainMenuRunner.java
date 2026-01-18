package com.um.helpdesk.demo;

import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;

@Component
@Order(1)
public class MainMenuRunner implements CommandLineRunner {

    private final UserService userService;
    private final UserManagementConsoleRunner userModule;
    private final NotificationConsoleRunner notificationModule;
    private final TicketSubmissionConsoleRunner ticketModule;   // ✅ added

    private User currentUser = null;

    public MainMenuRunner(
            UserService userService,
            UserManagementConsoleRunner userModule,
            NotificationConsoleRunner notificationModule,
            TicketSubmissionConsoleRunner ticketModule          // ✅ added
    ) {
        this.userService = userService;
        this.userModule = userModule;
        this.notificationModule = notificationModule;
        this.ticketModule = ticketModule;                       // ✅ added
    }

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║               UNIVERSITY MALAYA HELPDESK SYSTEM            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Simple user selection (no password needed)
        if (!selectDemoUser(sc)) {
            System.out.println("User selection failed. System terminated.");
            sc.close();
            return;
        }

        boolean running = true;

        while (running) {
            displayRoleBasedMenu();
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            // Route based on user role
            switch (currentUser.getRole()) {
                case ADMIN -> running = handleAdminMenu(choice, sc);
                case STUDENT, STAFF -> running = handleStudentStaffMenu(choice, sc);
                case TECHNICIAN -> running = handleTechnicianMenu(choice, sc);
            }
        }

        System.out.println("\nSession ended. Goodbye, " + currentUser.getFullName() + "!");
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
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.print("Choose user (1-4): ");

        int choice = sc.nextInt();
        sc.nextLine();

        try {
            currentUser = switch (choice) {
                case 1 -> userService.getUserById(1L);  // Admin
                case 2 -> userService.getUserById(2L);  // Student
                case 3 -> userService.getUserById(3L);  // Staff
                case 4 -> userService.getUserById(4L);  // Technician
                default -> null;
            };

            if (currentUser == null) {
                System.out.println("\nInvalid selection.\n");
                return false;
            }

            System.out.println("\nSelected: " + currentUser.getFullName());
            System.out.println("   Role: " + currentUser.getRole());
            System.out.println();
            return true;

        } catch (RuntimeException e) {
            System.out.println("\n Error: " + e.getMessage());
            System.out.println("TIP: Make sure test data is initialized.\n");
            return false;
        }
    }

    // ==================== ROLE-BASED MENUS ====================

    private void displayRoleBasedMenu() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
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

    // ==================== ADMIN MENU ====================

    private void displayAdminMenu() {
        System.out.println("                   ADMIN MENU                              ");
        System.out.println("───────────────────────────────────────────────────────────");
        System.out.println("1. User Management Module                                  ");
        System.out.println("2. Reporting Module                                        ");
        System.out.println("3. View All Tickets (Admin View)                           ");
        System.out.println("4. Notification Module                                     ");
        System.out.println("5. Ticket Submission Module                                "); // ✅ added
    }

    private boolean handleAdminMenu(int choice, Scanner sc) {
        switch (choice) {
            case 1 -> userModule.runUserManagement(sc, currentUser);
            case 2 -> System.out.println("\nReporting Module (teammate will implement)\n");
            case 3 -> System.out.println("\nView All Tickets (teammate will implement)\n");
            case 4 -> notificationModule.runNotificationManagement(sc, currentUser);
            case 5 -> ticketModule.run(currentUser.getId()); // ✅ added
            case 0 -> {
                return false;  // Exit
            }
            default -> System.out.println("\nInvalid option.\n");
        }
        return true;
    }

    // ==================== STUDENT/STAFF MENU ====================

    private void displayStudentStaffMenu() {
        System.out.println("                  STUDENT/STAFF MENU                  ");
        System.out.println("──────────────────────────────────────────────────────");
        System.out.println("1. Lodge New Ticket                                   ");
        System.out.println("2. View My Tickets                                    ");
        System.out.println("3. My Profile                                         ");
        System.out.println("4. My Notifications                                   ");
    }

    private boolean handleStudentStaffMenu(int choice, Scanner sc) {
        switch (choice) {
            case 1 -> ticketModule.run(currentUser.getId()); // ✅ Lodge + include feedback check inside module
            case 2 -> ticketModule.run(currentUser.getId()); // ✅ View/Track inside module
            case 3 -> viewMyProfile();
            case 4 -> notificationModule.runNotificationManagement(sc, currentUser);
            case 0 -> {
                return false;  // Exit
            }
            default -> System.out.println("\n Invalid option.\n");
        }
        return true;
    }

    // ==================== TECHNICIAN MENU ====================

    private void displayTechnicianMenu() {
        System.out.println("                 TECHNICIAN MENU                        ");
        System.out.println("────────────────────────────────────────────────────────");
        System.out.println("  1. View Assigned Tickets                              ");
        System.out.println("  2. Claim New Ticket                                   ");
        System.out.println("  3. Transfer Ticket to Department                      ");
        System.out.println("  4. My Profile                                         ");
        System.out.println("  5. My Notifications                                   ");
    }

    private boolean handleTechnicianMenu(int choice, Scanner sc) {
        switch (choice) {
            case 1 -> System.out.println("\nAssigned Tickets (teammate will implement)\n");
            case 2 -> System.out.println("\nClaim Ticket (teammate will implement)\n");
            case 3 -> System.out.println("\nTransfer Ticket (teammate will implement)\n");
            case 4 -> viewMyProfile();
            case 5 -> notificationModule.runNotificationManagement(sc, currentUser);
            case 0 -> {
                return false;  // Exit
            }
            default -> System.out.println("\nInvalid option.\n");
        }
        return true;
    }

    // ==================== COMMON FUNCTIONS ====================

    private void viewMyProfile() {
        System.out.println("\n═════════════════════════════════════════════════════════");
        System.out.println("                      MY PROFILE                           ");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("ID:          " + currentUser.getId());
        System.out.println("Full Name:   " + currentUser.getFullName());
        System.out.println("Email:       " + currentUser.getEmail());
        System.out.println("Phone:       " + (currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "N/A"));
        System.out.println("Role:        " + currentUser.getRole());
        System.out.println("Status:      " + (currentUser.isActive() ? "Active" : "Inactive"));

        if (currentUser instanceof Student s) {
            System.out.println("\n--- Student Details ---");
            System.out.println("Student ID:  " + (s.getStudentId() != null ? s.getStudentId() : "N/A"));
            System.out.println("Faculty:     " + (s.getFaculty() != null ? s.getFaculty() : "N/A"));
            System.out.println("Program:     " + (s.getProgram() != null ? s.getProgram() : "N/A"));
        } else if (currentUser instanceof TechnicianSupportStaff t) {
            System.out.println("\n--- Technician Details ---");
            System.out.println("Staff ID:    " + (t.getStaffId() != null ? t.getStaffId() : "N/A"));
            System.out.println("Specialization: " + (t.getSpecialization() != null ? t.getSpecialization() : "N/A"));
            System.out.println("Department:  " + (t.getDepartment() != null ? t.getDepartment().getName() : "N/A"));
        } else if (currentUser instanceof Staff st) {
            System.out.println("\n--- Staff Details ---");
            System.out.println("Staff ID:    " + (st.getStaffId() != null ? st.getStaffId() : "N/A"));
            System.out.println("Department:  " + (st.getDepartment() != null ? st.getDepartment().getName() : "N/A"));
        } else if (currentUser instanceof Administrator a) {
            System.out.println("\n--- Administrator Details ---");
            System.out.println("Admin Level: " + (a.getAdminLevel() != null ? a.getAdminLevel() : "N/A"));
        }

        System.out.println();
    }
}
