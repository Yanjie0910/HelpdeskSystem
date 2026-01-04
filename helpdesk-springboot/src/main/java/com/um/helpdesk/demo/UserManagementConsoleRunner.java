package com.um.helpdesk.demo;

import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order; 
import org.springframework.stereotype.Component;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;

@Component
@Order(2) 

public class UserManagementConsoleRunner implements CommandLineRunner {

    private final UserService userService;

    public UserManagementConsoleRunner(UserService userService) {
        this.userService = userService;
    }
    

 public void runUserManagement(Scanner sc, User currentUser) {
        
        // Permission check
        if (currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("\n Access Denied: Only administrators can access User Management.\n");
            return;
        }
        
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println("              USER MANAGEMENT MODULE                        ");
        System.out.println(" ═══════════════════════════════════════════════════════════");
        
        boolean back = false;
        
        while (!back) {
            displayUserManagementMenu();
            System.out.print("Choose option: ");
            
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch (choice) {
                case 1 -> manageUserProfiles(sc, currentUser);
                case 2 -> manageRolesAndPermissions(sc, currentUser);
                case 3 -> manageDepartmentAssignments(sc);
                case 4 -> viewUserActivityAndAuditLogs(sc);
                case 0 -> back = true;
                default -> System.out.println("\nInvalid option.\n");
            }
        }
    }

    private void displayUserManagementMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("              USER MANAGEMENT MENU                           ");
        System.out.println("  ═══════════════════════════════════════════════════════════");
        System.out.println("  1. Manage User Profiles (CRUD)");
        System.out.println("  2. Manage User Roles and Permissions  ");
        System.out.println("  3. Manage Department Assignments ");
        System.out.println("  4. View User Activity and Audit Logs");
        System.out.println("  0. Back to Main Menu ");
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    // ==================== FUNCTIONALITY 1: MANAGE USER PROFILES ====================
    private void manageUserProfiles(Scanner sc, User currentUser) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("            FUNCTIONALITY 1: MANAGE USER PROFILES         ");
        System.out.println("══════════════════════════════════════════════════════════");
        
        boolean back = false;
        while (!back) {
            System.out.println("1.Create New User");
            System.out.println("2.View All Users  ");
            System.out.println("3.View User by ID ");
            System.out.println("4.Update User Information");
            System.out.println("5.Delete User");
            System.out.println("0.Back to Main Menu");
            System.out.print("Choose option: ");
            
            int choice = sc.nextInt();
            sc.nextLine();
            
            switch (choice) {
                case 1 -> createUser(sc);
                case 2 -> viewAllUsers();
                case 3 -> viewUserById(sc);
                case 4 -> updateUser(sc);
                case 5 -> deleteUser(sc);
                case 0 -> back = true;
                default -> System.out.println("\n Invalid option.\n");
            }
        }
    }

    private void createUser(Scanner sc) {
        System.out.println("\n--- CREATE NEW USER ---");
        System.out.println("Select User Type:");
        System.out.println("1. Student");
        System.out.println("2. Staff");
        System.out.println("3. Technician");
        System.out.println("4. Administrator");
        System.out.print("User Type: ");
        
        int type = sc.nextInt();
        sc.nextLine();
        
        System.out.print("Email: ");
        String email = sc.nextLine();
        
        System.out.print("Full Name: ");
        String fullName = sc.nextLine();
        
        System.out.print("Phone Number (optional, press Enter to skip): ");
        String phone = sc.nextLine();
        
        System.out.print("Password: ");
        String password = sc.nextLine();
        
        User user = null;
        
        switch (type) {
            case 1 -> {
                Student student = new Student();
                student.setEmail(email);
                student.setFullName(fullName);
                student.setPhoneNumber(phone.isEmpty() ? null : phone);
                student.setPassword(password);
                student.setRole(UserRole.STUDENT);
                
                System.out.print("Student ID (optional): ");
                String studentId = sc.nextLine();
                student.setStudentId(studentId.isEmpty() ? null : studentId);
                
                System.out.print("Faculty (optional): ");
                String faculty = sc.nextLine();
                student.setFaculty(faculty.isEmpty() ? null : faculty);
                
                System.out.print("Program (optional): ");
                String program = sc.nextLine();
                student.setProgram(program.isEmpty() ? null : program);
                
                user = student;
            }
            case 2 -> {
                Staff staff = new Staff();
                staff.setEmail(email);
                staff.setFullName(fullName);
                staff.setPhoneNumber(phone.isEmpty() ? null : phone);
                staff.setPassword(password);
                staff.setRole(UserRole.STAFF);
                
                System.out.print("Staff ID (optional): ");
                String staffId = sc.nextLine();
                staff.setStaffId(staffId.isEmpty() ? null : staffId);
                
                user = staff;
            }
            case 3 -> {
                TechnicianSupportStaff technician = new TechnicianSupportStaff();
                technician.setEmail(email);
                technician.setFullName(fullName);
                technician.setPhoneNumber(phone.isEmpty() ? null : phone);
                technician.setPassword(password);
                technician.setRole(UserRole.TECHNICIAN);
                
                System.out.print("Staff ID (optional): ");
                String staffId = sc.nextLine();
                technician.setStaffId(staffId.isEmpty() ? null : staffId);
                
                System.out.print("Specialization (optional): ");
                String specialization = sc.nextLine();
                technician.setSpecialization(specialization.isEmpty() ? null : specialization);
                
                user = technician;
            }
            case 4 -> {
                Administrator admin = new Administrator();
                admin.setEmail(email);
                admin.setFullName(fullName);
                admin.setPhoneNumber(phone.isEmpty() ? null : phone);
                admin.setPassword(password);
                admin.setRole(UserRole.ADMIN);
                
                System.out.print("Admin Level (optional): ");
                String adminLevel = sc.nextLine();
                admin.setAdminLevel(adminLevel.isEmpty() ? null : adminLevel);
                
                user = admin;
            }
            default -> {
                System.out.println("Invalid user type.");
                return;
            }
        }
        
        User saved = userService.createUser(user);
        System.out.println("\n User created successfully!");
        System.out.println("   ID: " + saved.getId());
        System.out.println("   Name: " + saved.getFullName());
        System.out.println("   Role: " + saved.getRole());
        System.out.println();
    }

    private void viewAllUsers() {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("                     ALL USERS                            ");
        System.out.println("  ════════════════════════════════════════════════════════");
        
        var users = userService.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("No users found in the system.\n");
            return;
        }
        
        System.out.println(String.format("%-5s %-25s %-35s %-12s %-8s", 
            "ID", "NAME", "EMAIL", "ROLE", "STATUS"));
        System.out.println("─".repeat(90));
        
        for (User u : users) {
            System.out.println(String.format("%-5d %-25s %-35s %-12s %-8s",
                u.getId(),
                truncate(u.getFullName(), 25),
                truncate(u.getEmail(), 35),
                u.getRole(),
                u.isActive() ? "Active" : "Inactive"
            ));
        }
        System.out.println("\nTotal users: " + users.size() + "\n");
    }

    private void viewUserById(Scanner sc) {
        System.out.print("\nEnter User ID: ");
        Long id = sc.nextLong();
        sc.nextLine();
        
        try {
            User user = userService.getUserById(id);
            
            System.out.println("\n═════════════════════════════════════════════════════════");
            System.out.println("                    USER DETAILS                           ");
            System.out.println(" ══════════════════════════════════════════════════════════");
            System.out.println("ID:          " + user.getId());
            System.out.println("Full Name:   " + user.getFullName());
            System.out.println("Email:       " + user.getEmail());
            System.out.println("Phone:       " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A"));
            System.out.println("Role:        " + user.getRole());
            System.out.println("Status:      " + (user.isActive() ? "Active" : "Inactive"));
            System.out.println("Created:     " + user.getCreatedAt());
            System.out.println("Updated:     " + user.getUpdatedAt());
            
            // Type-specific details
            if (user instanceof Student s) {
                System.out.println("\n--- Student Details ---");
                System.out.println("Student ID:  " + (s.getStudentId() != null ? s.getStudentId() : "N/A"));
                System.out.println("Faculty:     " + (s.getFaculty() != null ? s.getFaculty() : "N/A"));
                System.out.println("Program:     " + (s.getProgram() != null ? s.getProgram() : "N/A"));
            } else if (user instanceof TechnicianSupportStaff t) {
                System.out.println("\n--- Technician Details ---");
                System.out.println("Staff ID:    " + (t.getStaffId() != null ? t.getStaffId() : "N/A"));
                System.out.println("Specialization: " + (t.getSpecialization() != null ? t.getSpecialization() : "N/A"));
                System.out.println("Department:  " + (t.getDepartment() != null ? t.getDepartment().getName() : "N/A"));
            } else if (user instanceof Staff s) {
                System.out.println("\n--- Staff Details ---");
                System.out.println("Staff ID:    " + (s.getStaffId() != null ? s.getStaffId() : "N/A"));
                System.out.println("Department:  " + (s.getDepartment() != null ? s.getDepartment().getName() : "N/A"));
            } else if (user instanceof Administrator a) {
                System.out.println("\n--- Administrator Details ---");
                System.out.println("Admin Level: " + (a.getAdminLevel() != null ? a.getAdminLevel() : "N/A"));
            }
            System.out.println();
            
        } catch (RuntimeException e) {
            System.out.println("\n " + e.getMessage() + "\n");
        }
    }

    private void updateUser(Scanner sc) {
        System.out.print("\nEnter User ID to update: ");
        Long id = sc.nextLong();
        sc.nextLine();
        
        try {
            User existing = userService.getUserById(id);
            
            System.out.println("\n--- Current Information ---");
            System.out.println("Name: " + existing.getFullName());
            System.out.println("Email: " + existing.getEmail());
            System.out.println("Phone: " + (existing.getPhoneNumber() != null ? existing.getPhoneNumber() : "N/A"));
            
            System.out.println("\n--- Enter New Information (press Enter to keep current) ---");
            
            System.out.print("New Full Name: ");
            String newName = sc.nextLine();
            if (!newName.isEmpty()) {
                existing.setFullName(newName);
            }
            
            System.out.print("New Email: ");
            String newEmail = sc.nextLine();
            if (!newEmail.isEmpty()) {
                existing.setEmail(newEmail);
            }
            
            System.out.print("New Phone: ");
            String newPhone = sc.nextLine();
            if (!newPhone.isEmpty()) {
                existing.setPhoneNumber(newPhone);
            }
            
            User updated = userService.updateUser(id, existing);
            
            System.out.println("\n User updated successfully!");
            System.out.println("   Name: " + updated.getFullName());
            System.out.println("   Email: " + updated.getEmail());
            System.out.println();
            
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    private void deleteUser(Scanner sc) {
        System.out.print("\nEnter User ID to delete: ");
        Long id = sc.nextLong();
        sc.nextLine();
        
        try {
            User user = userService.getUserById(id);
            System.out.println("\nWARNING: You are about to delete:");
            System.out.println("   ID: " + user.getId());
            System.out.println("   Name: " + user.getFullName());
            System.out.println("   Email: " + user.getEmail());
            
            System.out.print("\n❓ Are you sure? (yes/no): ");
            String confirm = sc.nextLine();
            
            if (confirm.equalsIgnoreCase("yes")) {
                userService.deleteUser(id);
                System.out.println("\n User deleted successfully!\n");
            } else {
                System.out.println("\n Deletion cancelled.\n");
            }
            
        } catch (RuntimeException e) {
            System.out.println("\n " + e.getMessage() + "\n");
        }
    }

    // ==================== FUNCTIONALITY 2: MANAGE ROLES AND PERMISSIONS ====================
    private void manageRolesAndPermissions(Scanner sc,User currentUser) {
        System.out.println("\n═════════════════════════════════════════════════════");
        System.out.println("       FUNCTIONALITY 2: MANAGE ROLES AND PERMISSIONS   ");
        System.out.println("═══════════════════════════════════════════════════════");
        
        System.out.print("Enter Target User ID (whose role you want to change): ");
        Long targetId = sc.nextLong();
        sc.nextLine();
        
        System.out.println("\n--- Available Roles ---");
        System.out.println("1. STUDENT");
        System.out.println("2. STAFF");
        System.out.println("3. TECHNICIAN");
        System.out.println("4. ADMIN");
        System.out.print("Select New Role (1-4): ");
        
        int roleChoice = sc.nextInt();
        sc.nextLine();
        
        UserRole newRole = switch (roleChoice) {
            case 1 -> UserRole.STUDENT;
            case 2 -> UserRole.STAFF;
            case 3 -> UserRole.TECHNICIAN;
            case 4 -> UserRole.ADMIN;
            default -> null;
        };
        
        if (newRole == null) {
            System.out.println("\nInvalid role selection.\n");
            return;
        }
        
        try {
            userService.assignRole(currentUser.getId(), targetId, newRole);
            System.out.println("\nRole assigned successfully!");
            System.out.println("   User ID " + targetId + " is now: " + newRole);
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    // ==================== FUNCTIONALITY 3: MANAGE DEPARTMENT ASSIGNMENTS ====================
    private void manageDepartmentAssignments(Scanner sc) {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("     FUNCTIONALITY 3: MANAGE DEPARTMENT ASSIGNMENTS      ");
        System.out.println(" ════════════════════════════════════════════════════════");
        
        System.out.print("\nEnter User ID (Staff/Technician only): ");
        Long userId = sc.nextLong();
        sc.nextLine();
        
        System.out.print("nter Department ID: ");
        Long deptId = sc.nextLong();
        sc.nextLine();
        
        try {
            userService.assignDepartment(userId, deptId);
            System.out.println("\nDepartment assigned successfully!");
            System.out.println("   User ID " + userId + " → Department ID " + deptId);
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage() + "\n");
        }
    }

    // ==================== FUNCTIONALITY 4: VIEW USER ACTIVITY AND AUDIT LOGS ====================
    private void viewUserActivityAndAuditLogs(Scanner sc) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("     FUNCTIONALITY 4: VIEW USER ACTIVITY AND AUDIT LOGS   ");
        System.out.println("══════════════════════════════════════════════════════════");
        
        System.out.print("\nEnter User ID to view activity: ");
        Long userId = sc.nextLong();
        sc.nextLine();
        
        try {
            String activity = userService.getUserActivity(userId);
            System.out.println("\n" + activity);
        } catch (RuntimeException e) {
            System.out.println("\n " + e.getMessage() + "\n");
        }
    }

    // Helper method
    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		
	}
}