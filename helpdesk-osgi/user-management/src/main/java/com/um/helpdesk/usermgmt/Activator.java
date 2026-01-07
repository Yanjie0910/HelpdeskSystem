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

    @Override
    public void start(BundleContext context) throws Exception {
        	System.out.println("Starting User Management Component...");

        // Initialize JPA
        emf = Persistence.createEntityManagerFactory("helpdesk-pu");
        em = emf.createEntityManager();
        System.out.println("JPA initialized\n");

        // Create service implementation
        UserManagementServiceImpl serviceImpl = new UserManagementServiceImpl(em);
        System.out.println("Service created\n");

        if (context != null) {
            Dictionary<String, String> props = new Hashtable<>();
            props.put("component", "user-management");
            props.put("version", "1.0.0");

            serviceRegistration = context.registerService(
                UserManagementService.class,
                serviceImpl,
                props
            );
            System.out.println("Service registered successfully\n");
        } else {
            System.out.println("OSGi context not available (standalone mode)");
            System.out.println("Service registration skipped\n");
        }

        // Initialize test data
        initializeTestData(serviceImpl);
        System.out.println("Test data created\n");

        System.out.println("-------------------------------------");
        System.out.println("|   USER MANAGEMENT COMPONENT       |");
        System.out.println("-------------------------------------\n");

        // Run console demo
        runConsoleDemo(serviceImpl);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping User Management Component...");

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            System.out.println("Service unregistered");
        }

        if (em != null && em.isOpen()) {
            em.close();
            System.out.println("EntityManager closed");
        }

        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println(" EntityManagerFactory closed");
        }

        System.out.println("User Management Component stopped.\n");
    }

    private void initializeTestData(UserManagementServiceImpl service) {
        DepartmentRepository deptRepo = new DepartmentRepository(em);

        // Create Departments
        Department itDept = new Department("Information Technology", "IT");
        itDept.setDescription("IT Support and Infrastructure");
        deptRepo.save(itDept);
        System.out.println("Created Department: IT (ID=1)");

        Department financeDept = new Department("Finance", "FIN");
        financeDept.setDescription("Financial Services");
        deptRepo.save(financeDept);
        System.out.println("Created Department: Finance (ID=2)");

        Department hrDept = new Department("Human Resources", "HR");
        hrDept.setDescription("Human Resources Department");
        deptRepo.save(hrDept);
        System.out.println("Created Department: HR (ID=3)");

        // Create Admin
        Administrator admin = new Administrator();
        admin.setEmail("admin@um.edu.my");
        admin.setFullName("Dr. World");
        admin.setPassword("admin123");
        admin.setPhoneNumber("0123456789");
        admin.setRole(UserRole.ADMIN);
        admin.setAdminLevel("Super Admin");
        service.createUser(admin);
        System.out.println("Created Administrator: Dr. World (ID=1)");

        // Create Student
        Student student = new Student();
        student.setEmail("lily@student.um.edu.my");
        student.setFullName("Lily Tan");
        student.setPassword("student123");
        student.setPhoneNumber("0123456789");
        student.setRole(UserRole.STUDENT);
        student.setStudentId("S2193570");
        student.setFaculty("Faculty of Computer Science");
        student.setProgram("Bachelor of Computer Science");
        service.createUser(student);
        System.out.println("Created Student: Lily Tan (ID=2)");

        // Create Staff
        Staff staff = new Staff();
        staff.setEmail("staff@um.edu.my");
        staff.setFullName("Muthu");
        staff.setPassword("staff123");
        staff.setPhoneNumber("0187654321");
        staff.setRole(UserRole.STAFF);
        staff.setStaffId("STAFF001");
        service.createUser(staff);
        System.out.println("Created Staff: Muthu (ID=3)");

        // Create Technician
        TechnicianSupportStaff technician = new TechnicianSupportStaff();
        technician.setEmail("tech@um.edu.my");
        technician.setFullName("Bob Lee");
        technician.setPassword("tech123");
        technician.setPhoneNumber("0176543210");
        technician.setRole(UserRole.TECHNICIAN);
        technician.setStaffId("TECH001");
        technician.setSpecialization("Network");
        service.createUser(technician);
        System.out.println("Created Technician: Bob Lee (ID=4)");
    }


    private void runConsoleDemo(UserManagementServiceImpl service) {
        Scanner sc = new Scanner(System.in);
        System.out.println("-------------------USER MANAGEMENT MODULE------------------------------");
        boolean running = true;

        while (running) {
            displayMenu();
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewAllUsers(service);
                case 2 -> viewUserById(service, sc);
                case 3 -> createUser(service, sc);
                case 4 -> updateUser(service, sc);
                case 5 -> assignRole(service, sc);
                case 6 -> assignDepartment(service, sc);
                case 7 -> viewUserActivity(service, sc);
                case 8 -> deleteUser(service, sc);
                case 0 -> running = false;
                default -> System.out.println("\nInvalid option.\n");
            }
        }

        System.out.println("\n✓ Demo ended. Bundle remains active.\n");
    }

    private void displayMenu() {
        System.out.println("┌────────────────────────────────────────────────────────┐");
        System.out.println("│              USER MANAGEMENT MENU                      │");
        System.out.println("├────────────────────────────────────────────────────────┤");
        System.out.println("│  1. View All Users                                     │");
        System.out.println("│  2. View User by ID                                    │");
        System.out.println("│  3. Create New User                                    │");
        System.out.println("│  4. Update User                                        │");
        System.out.println("│  5. Assign Role (Admin only)                           │");
        System.out.println("│  6. Assign Department                                  │");
        System.out.println("│  7. View User Activity                                 │");
        System.out.println("│  8. Delete User                                        │");
        System.out.println("│  0. Exit Demo                                          │");
        System.out.println("└────────────────────────────────────────────────────────┘");
    }

    // Demo menu handlers
    private void viewAllUsers(UserManagementServiceImpl service) {
        System.out.println("\n════════════════ ALL USERS ════════════════");
        var users = service.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("No users found.\n");
            return;
        }
        
        System.out.printf("%-5s %-25s %-30s %-12s%n", "ID", "NAME", "EMAIL", "ROLE");
        System.out.println("─".repeat(75));
        
        for (User u : users) {
            System.out.printf("%-5d %-25s %-30s %-12s%n",
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole()
            );
        }
        System.out.println("\nTotal: " + users.size() + " user(s)\n");
    }

    private void viewUserById(UserManagementServiceImpl service, Scanner sc) {
        System.out.print("\nEnter User ID: ");
        Long id = sc.nextLong();
        sc.nextLine();
        
        try {
            User user = service.getUserById(id);
            System.out.println("\n════════════════ USER DETAILS ════════════════");
            System.out.println("ID:          " + user.getId());
            System.out.println("Name:        " + user.getFullName());
            System.out.println("Email:       " + user.getEmail());
            System.out.println("Phone:       " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A"));
            System.out.println("Role:        " + user.getRole());
            System.out.println("Active:      " + (user.isActive() ? "Yes" : "No"));
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private void createUser(UserManagementServiceImpl service, Scanner sc) {
        System.out.println("\n--- CREATE USER ---");
        System.out.println("Select type: 1=Student, 2=Staff, 3=Technician, 4=Admin");
        System.out.print("Type: ");
        int type = sc.nextInt();
        sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Full Name: ");
        String fullName = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        User user = null;

        switch (type) {
            case 1 -> {
                Student s = new Student();
                s.setEmail(email);
                s.setFullName(fullName);
                s.setPassword(password);
                s.setRole(UserRole.STUDENT);
                user = s;
            }
            case 2 -> {
                Staff s = new Staff();
                s.setEmail(email);
                s.setFullName(fullName);
                s.setPassword(password);
                s.setRole(UserRole.STAFF);
                user = s;
            }
            case 3 -> {
                TechnicianSupportStaff t = new TechnicianSupportStaff();
                t.setEmail(email);
                t.setFullName(fullName);
                t.setPassword(password);
                t.setRole(UserRole.TECHNICIAN);
                user = t;
            }
            case 4 -> {
                Administrator a = new Administrator();
                a.setEmail(email);
                a.setFullName(fullName);
                a.setPassword(password);
                a.setRole(UserRole.ADMIN);
                user = a;
            }
            default -> {
                System.out.println("\nInvalid type.\n");
                return;
            }
        }

        User saved = service.createUser(user);
        System.out.println("\n✓ User created! ID: " + saved.getId() + "\n");
    }

    private void updateUser(UserManagementServiceImpl service, Scanner sc) {
        System.out.print("\nEnter User ID to update: ");
        Long id = sc.nextLong();
        sc.nextLine();

        try {
            User existing = service.getUserById(id);
            System.out.println("Current name: " + existing.getFullName());

            System.out.print("New name (Enter to skip): ");
            String newName = sc.nextLine();
            if (!newName.isEmpty()) {
                existing.setFullName(newName);
            }

            User updated = service.updateUser(id, existing);
            System.out.println("\n✓ User updated!\n");

        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private void assignRole(UserManagementServiceImpl service, Scanner sc) {
        System.out.print("\nEnter Admin ID: ");
        Long adminId = sc.nextLong();
        sc.nextLine();

        System.out.print("Enter Target User ID: ");
        Long targetId = sc.nextLong();
        sc.nextLine();

        System.out.println("Roles: 1=STUDENT, 2=STAFF, 3=TECHNICIAN, 4=ADMIN");
        System.out.print("Select: ");
        int choice = sc.nextInt();
        sc.nextLine();

        UserRole role = switch (choice) {
            case 1 -> UserRole.STUDENT;
            case 2 -> UserRole.STAFF;
            case 3 -> UserRole.TECHNICIAN;
            case 4 -> UserRole.ADMIN;
            default -> null;
        };

        if (role == null) {
            System.out.println("\nInvalid role.\n");
            return;
        }

        try {
            service.assignRole(adminId, targetId, role);
            System.out.println("\nRole assigned!\n");
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private void assignDepartment(UserManagementServiceImpl service, Scanner sc) {
        System.out.print("\nEnter User ID: ");
        Long userId = sc.nextLong();
        sc.nextLine();

        System.out.print("Enter Department ID (1=IT, 2=Finance, 3=HR): ");
        Long deptId = sc.nextLong();
        sc.nextLine();

        try {
            service.assignDepartment(userId, deptId);
            System.out.println("\n✓ Department assigned!\n");
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private void viewUserActivity(UserManagementServiceImpl service, Scanner sc) {
        System.out.print("\nEnter User ID: ");
        Long id = sc.nextLong();
        sc.nextLine();

        try {
            String activity = service.getUserActivity(id);
            System.out.println("\n" + activity);
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private void deleteUser(UserManagementServiceImpl service, Scanner sc) {
        System.out.print("\nEnter User ID to delete: ");
        Long id = sc.nextLong();
        sc.nextLine();

        System.out.print("Confirm (yes/no): ");
        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            try {
                service.deleteUser(id);
                System.out.println("\n✓ User deleted!\n");
            } catch (RuntimeException e) {
                System.out.println("\n" + e.getMessage() + "\n");
            }
        } else {
            System.out.println("\nCancelled.\n");
        }
    }
}
