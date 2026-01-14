package com.um.helpdesk.usermgmt;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserManagementService;
import com.um.helpdesk.usermgmt.impl.UserManagementServiceImpl;
import com.um.helpdesk.usermgmt.repository.DepartmentRepository;
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
    private ServiceRegistration<UserManagementService> serviceRegistration;
    private boolean running = true;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println(">>> User Management Component: Starting...");

        // Kita jalankan logik berat dalam Thread berbeza supaya Karaf tak HANG
        new Thread(() -> {
            try {
                // FIX: Beritahu Java guna ClassLoader bundle ini untuk cari Hibernate
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                System.out.println(">>> JPA: Initializing EntityManagerFactory...");
                emf = Persistence.createEntityManagerFactory("helpdesk-pu");
                em = emf.createEntityManager();
                System.out.println(">>> JPA: Initialized successfully.");

                UserManagementServiceImpl serviceImpl = new UserManagementServiceImpl(em);

                // Register OSGi Service
                Dictionary<String, String> props = new Hashtable<>();
                props.put("component", "user-management");
                serviceRegistration = context.registerService(UserManagementService.class, serviceImpl, props);
                System.out.println(">>> OSGi: Service registered.");

                initializeTestData(serviceImpl);

                // Jalankan Menu Console
                runConsoleDemo(serviceImpl);

            } catch (Exception e) {
                System.err.println("!!! ERROR in User Management Activator Thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        System.out.println(">>> User Management Activator thread launched. Status will be ACTIVE soon.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        running = false;
        System.out.println("Stopping User Management Component...");
        if (serviceRegistration != null)
            serviceRegistration.unregister();
        if (em != null && em.isOpen())
            em.close();
        if (emf != null && emf.isOpen())
            emf.close();
    }

    // --- Paste balik semua method initializeTestData, runConsoleDemo, displayMenu
    // kau kat bawah ni ---
    // (Kod handlers viewAllUsers, createUser, etc. tidak berubah)

    private void initializeTestData(UserManagementServiceImpl service) {
        // ... (sama macam kod asal kau) ...
        System.out.println(">>> Test Data: Sample users created.");
    }

    private void runConsoleDemo(UserManagementServiceImpl service) {
        Scanner sc = new Scanner(System.in);
        while (running) {
            displayMenu();
            System.out.print("Choose option: ");
            try {
                if (sc.hasNextInt()) {
                    int choice = sc.nextInt();
                    sc.nextLine();
                    if (choice == 0)
                        break;
                    handleMenu(choice, service, sc);
                } else {
                    sc.nextLine();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleMenu(int choice, UserManagementServiceImpl service, Scanner sc) {
        // Pindahkan switch-case kau ke sini
        switch (choice) {
            case 1 -> viewAllUsers(service);
            // ... (tambah case lain)
        }
    }

    private void displayMenu() {
        System.out.println("\n--- USER MANAGEMENT MENU ---");
        System.out.println("1. View All Users");
        System.out.println("0. Exit Demo");
    }

    private void viewAllUsers(UserManagementServiceImpl service) {
        var users = service.getAllUsers();
        users.forEach(u -> System.out.println(u.getId() + ": " + u.getFullName() + " [" + u.getRole() + "]"));
    }
}