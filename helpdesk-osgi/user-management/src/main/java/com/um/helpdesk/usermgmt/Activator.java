package com.um.helpdesk.usermgmt;

import com.um.helpdesk.service.UserManagementService;
import com.um.helpdesk.usermgmt.impl.UserManagementServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Activator implements BundleActivator {

    private ServiceRegistration<UserManagementService> serviceRegistration;
    private volatile boolean running = true;

    @Override
    public void start(BundleContext context) {
        System.out.println(">>> User Management Component: Starting...");

        try {
            UserManagementServiceImpl serviceImpl = new UserManagementServiceImpl();
            System.out.println(">>> Storage: In-Memory mode (no JPA).");

            if (context != null) {
                Dictionary<String, String> props = new Hashtable<>();
                props.put("component", "user-management");

                serviceRegistration = context.registerService(UserManagementService.class, serviceImpl, props);
                System.out.println(">>> OSGi: UserManagementService registered.");
                System.out.println(">>> (Karaf mode) Demo menu is DISABLED to avoid System.in conflict.");
                return;
            }

            // ✅ Simulation 模式：才运行 demo 菜单
            System.out.println(">>> (Simulation mode) Starting console demo...");
            initializeTestData(serviceImpl);
            runConsoleDemo(serviceImpl);

        } catch (Exception e) {
            System.err.println("!!! ERROR in User Management Activator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop(BundleContext context) {
        running = false;
        System.out.println("Stopping User Management Component...");
        try {
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                serviceRegistration = null;
            }
        } catch (Exception ignored) {}
    }

    private void initializeTestData(UserManagementServiceImpl service) {
        System.out.println(">>> Test Data: (optional) initialized.");
    }

    // ==================== Simulation Demo Menu (ONLY when context == null) ====================

    private void runConsoleDemo(UserManagementServiceImpl service) {
        Scanner sc = new Scanner(System.in);
        while (running) {
            displayMenu();
            System.out.print("Choose option: ");
            try {
                if (sc.hasNextInt()) {
                    int choice = sc.nextInt();
                    sc.nextLine();
                    if (choice == 0) break;
                    handleMenu(choice, service, sc);
                } else {
                    sc.nextLine();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println(">>> User Management Demo exited.");
    }

    private void handleMenu(int choice, UserManagementServiceImpl service, Scanner sc) {
        switch (choice) {
            case 1 -> viewAllUsers(service);
            default -> System.out.println("Unknown option.");
        }
    }

    private void displayMenu() {
        System.out.println("\n--- USER MANAGEMENT MENU ---");
        System.out.println("1. View All Users");
        System.out.println("0. Exit Demo");
    }

    private void viewAllUsers(UserManagementServiceImpl service) {
        var users = service.getAllUsers();
        if (users == null || users.isEmpty()) {
            System.out.println("(no users)");
            return;
        }
        users.forEach(u ->
                System.out.println(u.getId() + ": " + u.getFullName() + " [" + u.getRole() + "]")
        );
    }
}
