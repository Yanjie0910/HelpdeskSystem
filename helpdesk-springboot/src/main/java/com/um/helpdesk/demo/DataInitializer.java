package com.um.helpdesk.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;
import com.um.helpdesk.service.TicketService;
import com.um.helpdesk.repository.DepartmentRepository;
import com.um.helpdesk.repository.TicketRepository;

import java.time.LocalDateTime;

@Component
@Order(0)
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final DepartmentRepository departmentRepository;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

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
        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        // --- 1. DEPARTMENTS ---
        Department itDept = new Department("Information Technology", "IT");
        departmentRepository.save(itDept);

        Department financeDept = new Department("Finance", "FIN");
        departmentRepository.save(financeDept);

        Department hrDept = new Department("Human Resources", "HR");
        departmentRepository.save(hrDept);

        Department facilitiesDept = new Department("Facilities", "FAC");
        departmentRepository.save(facilitiesDept);

        // --- 2. USERS ---
        Administrator admin = new Administrator();
        admin.setEmail("admin@um.edu.my"); admin.setFullName("Dr. World");
        admin.setPassword("admin123"); admin.setRole(UserRole.ADMIN);
        userService.createUser(admin);

        Student student = new Student();
        student.setEmail("lily@student.um.edu.my"); student.setFullName("Lily Tan");
        student.setPassword("student123"); student.setRole(UserRole.STUDENT);
        userService.createUser(student);

        Staff staff = new Staff();
        staff.setEmail("staff@um.edu.my"); staff.setFullName("Muthu");
        staff.setPassword("staff123"); staff.setRole(UserRole.STAFF);
        userService.createUser(staff);

        TechnicianSupportStaff tech1 = new TechnicianSupportStaff();
        tech1.setEmail("bob@um.edu.my"); tech1.setFullName("Bob Lee");
        tech1.setPassword("tech123"); tech1.setRole(UserRole.TECHNICIAN);
        tech1.setDepartment(itDept);
        userService.createUser(tech1);

        TechnicianSupportStaff tech2 = new TechnicianSupportStaff();
        tech2.setEmail("siti@um.edu.my"); tech2.setFullName("Siti Tech");
        tech2.setPassword("tech123"); tech2.setRole(UserRole.TECHNICIAN);
        tech2.setDepartment(itDept);
        userService.createUser(tech2);

        System.out.println("✓ Users & Departments Initialized");

        // --- 3. CURRENT TICKETS ---
        createTicket("WiFi Down", "Cannot connect to UM Guest", TicketPriority.HIGH, student);
        createTicket("Printer Jam", "Level 2 printer jammed", TicketPriority.LOW, staff);

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

    private void createHistoricalData(User student, User staff, TechnicianSupportStaff tech) {
        LocalDateTime now = LocalDateTime.now();

        // 3 Weeks Ago (2 Tickets)
        createPastTicket("Legacy System Login", TicketPriority.MEDIUM, student, now.minusWeeks(3));
        createPastTicket("Old Printer Issue", TicketPriority.LOW, staff, now.minusWeeks(3));

        // 2 Weeks Ago (4 Tickets)
        createPastTicket("Network Slowness", TicketPriority.HIGH, student, now.minusWeeks(2));
        createPastTicket("Projector Bulb", TicketPriority.MEDIUM, staff, now.minusWeeks(2));
        createPastTicket("Email Sync", TicketPriority.LOW, staff, now.minusWeeks(2));
        createPastTicket("VPN Access", TicketPriority.HIGH, staff, now.minusWeeks(2));

        // 1 Week Ago (3 Tickets)
        createPastTicket("Software Update", TicketPriority.LOW, student, now.minusWeeks(1));
        createPastTicket("Mouse Broken", TicketPriority.LOW, staff, now.minusWeeks(1));
        createPastTicket("Keyboard Stuck", TicketPriority.LOW, staff, now.minusWeeks(1));

        // FAILURE TEST: Urgent Ticket submitted 2 days ago (Open = Breach 24h SLA)
        Ticket failure = new Ticket();
        failure.setTitle("Server Room Overheat");
        failure.setDescription("AC failure in server room. Critical!");
        failure.setPriority(TicketPriority.URGENT);
        failure.setSubmittedBy(staff);
        failure.setStatus(TicketStatus.OPEN); // Still open!
        failure.setSubmittedAt(now.minusDays(2)); // 48 hours ago
        ticketRepository.save(failure); // Direct save to bypass auto-timestamping in service if exists

        // FAILURE TEST: High Ticket submitted 4 days ago, resolved today (Took 96h > 48h SLA)
        Ticket lateResolve = new Ticket();
        lateResolve.setTitle("Database Corruption");
        lateResolve.setPriority(TicketPriority.HIGH);
        lateResolve.setSubmittedBy(staff);
        lateResolve.setStatus(TicketStatus.CLOSED);
        lateResolve.setSubmittedAt(now.minusDays(4));
        lateResolve.setResolvedAt(now);
        lateResolve.setAssignedTo(tech);
        ticketRepository.save(lateResolve);
    }

    private void createPastTicket(String title, TicketPriority p, User u, LocalDateTime date) {
        Ticket t = new Ticket();
        t.setTitle(title);
        t.setPriority(p);
        t.setSubmittedBy(u);
        t.setStatus(TicketStatus.CLOSED);
        t.setSubmittedAt(date);
        t.setResolvedAt(date.plusHours(2)); // Resolved same day
        ticketRepository.save(t);
    }
}