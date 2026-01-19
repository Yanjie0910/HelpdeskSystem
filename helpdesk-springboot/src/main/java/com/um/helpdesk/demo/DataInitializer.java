package com.um.helpdesk.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;
import com.um.helpdesk.service.TicketService;
import com.um.helpdesk.repository.DepartmentRepository;

@Component
@Order(0) 
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final DepartmentRepository departmentRepository;
    private final TicketService ticketService;

    public DataInitializer(UserService userService, DepartmentRepository departmentRepository, TicketService ticketService) {
        this.userService = userService;
        this.departmentRepository=departmentRepository;
        this.ticketService = ticketService;
    }

    @Override
    public void run(String... args) {
        if (!userService.getAllUsers().isEmpty()) {
            return; 
        }
     // Create Department
        Department itDept = new Department("Information Technology", "IT");
        itDept.setDescription("IT Support and Infrastructure");
        departmentRepository.save(itDept);
        System.out.println("✓ Created Department: IT (ID=1)");

        Department financeDept = new Department("Finance", "FIN");
        financeDept.setDescription("Financial Services");
        departmentRepository.save(financeDept);
        System.out.println("✓ Created Department: Finance (ID=2)");

        Department hrDept = new Department("Human Resources", "HR");
        hrDept.setDescription("Human Resources Department");
        departmentRepository.save(hrDept);
        System.out.println("✓ Created Department: HR (ID=3)");

        Department facilitiesDept = new Department("Facilities Management", "FACILITIES");
        facilitiesDept.setDescription("Building and Facilities Maintenance");
        departmentRepository.save(facilitiesDept);
        System.out.println("✓ Created Department: Facilities (ID=4)");

        Department academicDept = new Department("Academic Affairs", "ACADEMIC");
        academicDept.setDescription("Academic Administration");
        departmentRepository.save(academicDept);
        System.out.println("✓ Created Department: Academic (ID=5)");

        System.out.println();

        // Create Admin
        Administrator admin = new Administrator();
        admin.setEmail("admin@um.edu.my");
        admin.setFullName("Dr. World");
        admin.setPassword("admin123");
        admin.setPhoneNumber("0123456789");
        admin.setRole(UserRole.ADMIN);
        admin.setAdminLevel("Super Admin");
        userService.createUser(admin);

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
        userService.createUser(student);

        // Create Staff
        Staff staff = new Staff();
        staff.setEmail("staff@um.edu.my");
        staff.setFullName("Muthu");
        staff.setPassword("staff123");
        staff.setPhoneNumber("0187654321");
        staff.setRole(UserRole.STAFF);
        staff.setStaffId("STAFF001");
        userService.createUser(staff);

        // Create Technician 1 (IT Department)
        TechnicianSupportStaff technician1 = new TechnicianSupportStaff();
        technician1.setEmail("tech@um.edu.my");
        technician1.setFullName("Bob Lee");
        technician1.setPassword("tech123");
        technician1.setPhoneNumber("0176543210");
        technician1.setRole(UserRole.TECHNICIAN);
        technician1.setStaffId("TECH001");
        technician1.setSpecialization("Network");
        technician1.setDepartment(itDept);
        userService.createUser(technician1);

        // Create Technician 2 (IT Department)
        TechnicianSupportStaff technician2 = new TechnicianSupportStaff();
        technician2.setEmail("siti.tech@um.edu.my");
        technician2.setFullName("Siti Software Expert");
        technician2.setPassword("tech123");
        technician2.setPhoneNumber("0123456788");
        technician2.setRole(UserRole.TECHNICIAN);
        technician2.setStaffId("TECH002");
        technician2.setSpecialization("Software Support");
        technician2.setDepartment(itDept);
        userService.createUser(technician2);

        // Create Technician 3 (Facilities Department)
        TechnicianSupportStaff technician3 = new TechnicianSupportStaff();
        technician3.setEmail("kumar.facilities@um.edu.my");
        technician3.setFullName("Kumar Facilities Manager");
        technician3.setPassword("tech123");
        technician3.setPhoneNumber("0123456777");
        technician3.setRole(UserRole.TECHNICIAN);
        technician3.setStaffId("FAC001");
        technician3.setSpecialization("Building Maintenance");
        technician3.setDepartment(facilitiesDept);
        userService.createUser(technician3);

        System.out.println("\n--- Creating Sample Tickets ---\n");

        // Create Sample Ticket 1
        Ticket ticket1 = new Ticket();
        ticket1.setTitle("Cannot connect to campus WiFi");
        ticket1.setDescription("My laptop cannot connect to the university WiFi network. It shows authentication error.");
        ticket1.setCategory("Network");
        ticket1.setPriority(TicketPriority.HIGH);
        ticket1.setSubmittedBy(student);
        ticket1.setStatus(TicketStatus.OPEN);
        ticketService.createTicket(ticket1);
        System.out.println("✓ Created Ticket: WiFi Connection Issue (ID=1)");

        // Create Sample Ticket 2
        Ticket ticket2 = new Ticket();
        ticket2.setTitle("Air conditioning not working in Room A301");
        ticket2.setDescription("The air conditioning unit in lecture room A301 has not been working for 2 days.");
        ticket2.setCategory("Facilities");
        ticket2.setPriority(TicketPriority.MEDIUM);
        ticket2.setSubmittedBy(student);
        ticket2.setStatus(TicketStatus.OPEN);
        ticketService.createTicket(ticket2);
        System.out.println("✓ Created Ticket: Air Conditioning Issue (ID=2)");

        // Create Sample Ticket 3
        Ticket ticket3 = new Ticket();
        ticket3.setTitle("Software installation request");
        ticket3.setDescription("Need to install MATLAB on computer lab machines for engineering course.");
        ticket3.setCategory("Software");
        ticket3.setPriority(TicketPriority.LOW);
        ticket3.setSubmittedBy(staff);
        ticket3.setStatus(TicketStatus.OPEN);
        ticketService.createTicket(ticket3);
        System.out.println("✓ Created Ticket: Software Installation (ID=3)");

        System.out.println("\n✓ Data initialization complete!\n");
    }
}