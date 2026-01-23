package com.um.helpdesk.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;
import com.um.helpdesk.service.TicketService;
import com.um.helpdesk.repository.DepartmentRepository;
import com.um.helpdesk.repository.TicketRepository; // <--- IMPORT THIS

import java.time.LocalDateTime;
import java.util.Random;

@Component
@Order(0)
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final DepartmentRepository departmentRepository;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository; // <--- ADD THIS

    // UPDATED CONSTRUCTOR
    public DataInitializer(UserService userService,
                           DepartmentRepository departmentRepository,
                           TicketService ticketService,
                           TicketRepository ticketRepository) {
        this.userService = userService;
        this.departmentRepository = departmentRepository;
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void run(String... args) {
        // Prevent duplicate data if DB exists
        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        // --- 1. CREATE DEPARTMENTS ---
        Department itDept = new Department("Information Technology", "IT");
        departmentRepository.save(itDept);

        Department financeDept = new Department("Finance", "FIN");
        departmentRepository.save(financeDept);

        Department hrDept = new Department("Human Resources", "HR");
        departmentRepository.save(hrDept);

        Department facilitiesDept = new Department("Facilities", "FAC");
        departmentRepository.save(facilitiesDept);

        System.out.println("✓ Departments Created");

        // --- 2. CREATE USERS ---
        // Admin
        Administrator admin = new Administrator();
        admin.setEmail("admin@um.edu.my"); admin.setFullName("Dr. World");
        admin.setPassword("admin123"); admin.setRole(UserRole.ADMIN);
        userService.createUser(admin);

        // Student
        Student student = new Student();
        student.setEmail("lily@student.um.edu.my"); student.setFullName("Lily Tan");
        student.setPassword("student123"); student.setRole(UserRole.STUDENT);
        userService.createUser(student);

        // Staff
        Staff staff = new Staff();
        staff.setEmail("staff@um.edu.my"); staff.setFullName("Muthu");
        staff.setPassword("staff123"); staff.setRole(UserRole.STAFF);
        userService.createUser(staff);

        // Technician 1 (IT)
        TechnicianSupportStaff tech1 = new TechnicianSupportStaff();
        tech1.setEmail("bob@um.edu.my"); tech1.setFullName("Bob Lee");
        tech1.setPassword("tech123"); tech1.setRole(UserRole.TECHNICIAN);
        tech1.setDepartment(itDept);
        userService.createUser(tech1);

        System.out.println("✓ Users Created");

        // --- 3. CREATE CURRENT TICKETS (The 3 you had) ---
        createTicket("WiFi Down", "Cannot connect to UM Guest", TicketPriority.HIGH, student);
        createTicket("Printer Jam", "Level 2 printer jammed", TicketPriority.LOW, staff);
        createTicket("Software Install", "Need MATLAB", TicketPriority.LOW, staff);

        // Create claimable tickets directly
        Ticket t1 = new Ticket();
        t1.setTitle("Laptop won't start");
        t1.setDescription("My laptop shows black screen");
        t1.setPriority(TicketPriority.HIGH);
        t1.setSubmittedBy(student);
        t1.setStatus(TicketStatus.OPEN);
        t1.setAssignedDepartment(itDept);
        t1.setSubmittedAt(LocalDateTime.now());
        ticketRepository.save(t1);

        Ticket t2 = new Ticket();
        t2.setTitle("Password reset");
        t2.setDescription("Forgot my password");
        t2.setPriority(TicketPriority.MEDIUM);
        t2.setSubmittedBy(staff);
        t2.setStatus(TicketStatus.OPEN);
        t2.setAssignedDepartment(itDept);
        t2.setSubmittedAt(LocalDateTime.now());
        ticketRepository.save(t2);

        // More OPEN tickets for testing
        Ticket t3 = new Ticket();
        t3.setTitle("Software installation");
        t3.setDescription("Need Office installed");
        t3.setPriority(TicketPriority.LOW);
        t3.setSubmittedBy(student);
        t3.setStatus(TicketStatus.OPEN);
        t3.setSubmittedAt(LocalDateTime.now());
        ticketRepository.save(t3);

        Ticket t4 = new Ticket();
        t4.setTitle("Email not working");
        t4.setDescription("Cannot send emails from Outlook");
        t4.setPriority(TicketPriority.HIGH);
        t4.setSubmittedBy(staff);
        t4.setStatus(TicketStatus.OPEN);
        t4.setSubmittedAt(LocalDateTime.now());
        ticketRepository.save(t4);

        Ticket t5 = new Ticket();
        t5.setTitle("Broken chair");
        t5.setDescription("Office chair broken, need replacement");
        t5.setPriority(TicketPriority.MEDIUM);
        t5.setSubmittedBy(student);
        t5.setStatus(TicketStatus.OPEN);
        t5.setSubmittedAt(LocalDateTime.now());
        ticketRepository.save(t5);

        // --- 4. HISTORICAL DATA FOR REPORTING (TRENDS & FAILURES) ---
        createHistoricalData(student, staff, tech1);

        System.out.println("✓ Data initialization complete with Historical Reporting Data!");
    }

    private void createTicket(String title, String desc, TicketPriority prio, User user) {
        Ticket t = new Ticket();
        t.setTitle(title);
        t.setDescription(desc);
        t.setPriority(prio);
        t.setSubmittedBy(user);
        ticketService.createTicket(t);
    }

    private void createClaimableTicket(String title, String desc, TicketPriority prio, User user, Department dept) {
        Ticket t = new Ticket();
        t.setTitle(title);
        t.setDescription(desc);
        t.setPriority(prio);
        t.setSubmittedBy(user);
        t.setStatus(TicketStatus.OPEN);
        t.setAssignedDepartment(dept);  // Assigned to department
        t.setAssignedTo(null);  // NOT assigned to any technician - claimable!
        t.setSubmittedAt(LocalDateTime.now());
        ticketRepository.save(t);
    }

    private void createHistoricalData(User student, User staff, TechnicianSupportStaff tech) {
        // --- 4. CREATE HISTORICAL DATA (FOR REPORTING DEMO) ---
        // This is the part you were missing!
        System.out.println("--- Generating Historical Data... ---");
        LocalDateTime now = LocalDateTime.now();

        // 3 Weeks Ago (Trend: Low)
        createPastTicket("Legacy System Login", TicketPriority.MEDIUM, student, tech1, now.minusWeeks(3));
        createPastTicket("Old Printer Issue", TicketPriority.LOW, staff, tech1, now.minusWeeks(3));

        // 2 Weeks Ago (Trend: Medium)
        createPastTicket("Network Slowness", TicketPriority.HIGH, student, tech1, now.minusWeeks(2));
        createPastTicket("Projector Bulb", TicketPriority.MEDIUM, staff, tech1, now.minusWeeks(2));
        createPastTicket("Email Sync", TicketPriority.LOW, staff, tech1, now.minusWeeks(2));
        createPastTicket("VPN Access", TicketPriority.HIGH, staff, tech1, now.minusWeeks(2));

        // 1 Week Ago (Trend: High)
        createPastTicket("Software Update", TicketPriority.LOW, student, tech1, now.minusWeeks(1));
        createPastTicket("Mouse Broken", TicketPriority.LOW, staff, tech1, now.minusWeeks(1));
        createPastTicket("Keyboard Stuck", TicketPriority.LOW, staff, tech1, now.minusWeeks(1));

        // SLA FAILURE TEST: Urgent Ticket submitted 2 days ago (Still Open = Breach)
        Ticket failure = new Ticket();
        failure.setTitle("Server Room Overheat");
        failure.setDescription("AC failure in server room. Critical! Temperature rising.");
        failure.setCategory("Facilities");
        failure.setPriority(TicketPriority.URGENT);
        failure.setSubmittedBy(staff);
        failure.setStatus(TicketStatus.OPEN); // Still open!
        failure.setSubmittedAt(now.minusDays(2)); // 48 hours ago
        ticketRepository.save(failure);

        // SLA FAILURE TEST: High Ticket submitted 4 days ago, resolved today (Took 96h > 48h SLA)
        Ticket lateResolve = new Ticket();
        lateResolve.setTitle("Database Corruption");
        lateResolve.setDescription("Database server crashed and needs recovery");
        lateResolve.setCategory("IT");
        lateResolve.setPriority(TicketPriority.HIGH);
        lateResolve.setSubmittedBy(staff);
        lateResolve.setStatus(TicketStatus.OPEN);
        lateResolve.setSubmittedAt(now.minusDays(4));
        lateResolve.setResolvedAt(now);
        lateResolve.setAssignedTo(tech1);
        ticketRepository.save(lateResolve);

        System.out.println("✓ Data initialization complete with Historical Reporting Data!");
    }

    // Helper for regular tickets
    private void createTicket(String title, String desc, TicketPriority prio, User user) {
        Ticket t = new Ticket();
        t.setTitle(title);
        t.setDescription(desc);
        t.setPriority(prio);
        t.setSubmittedBy(user);
        t.setStatus(TicketStatus.OPEN);
        ticketService.createTicket(t);
    }

    // Helper for historical tickets (Uses Repository to force past dates)
    private void createPastTicket(String title, TicketPriority p, User u, TechnicianSupportStaff tech, LocalDateTime date) {
        Ticket t = new Ticket();
        t.setTitle(title);
        t.setPriority(p);
        t.setSubmittedBy(u);
        t.setAssignedTo(tech);
        t.setStatus(TicketStatus.CLOSED);
        t.setSubmittedAt(date);
        t.setResolvedAt(date.plusHours(2)); // Resolved same day
        ticketRepository.save(t);
    }
}