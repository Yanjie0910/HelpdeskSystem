package com.um.helpdesk.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;
import com.um.helpdesk.repository.DepartmentRepository;

@Component
@Order(0) 
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final DepartmentRepository departmentRepository;

    public DataInitializer(UserService userService, DepartmentRepository departmentRepository) {
        this.userService = userService;
        this.departmentRepository=departmentRepository;
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

        // Create Technician
        TechnicianSupportStaff technician = new TechnicianSupportStaff();
        technician.setEmail("tech@um.edu.my");
        technician.setFullName("Bob Lee");
        technician.setPassword("tech123");
        technician.setPhoneNumber("0176543210");
        technician.setRole(UserRole.TECHNICIAN);
        technician.setStaffId("TECH001");
        technician.setSpecialization("Network");
        userService.createUser(technician);
    }
}