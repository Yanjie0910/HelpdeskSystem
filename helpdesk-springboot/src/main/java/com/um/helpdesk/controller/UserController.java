package com.um.helpdesk.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        System.out.println("GET /api/users - Fetching all users");
        List<User> users = userService.getAllUsers();
        System.out.println("✓ Found " + users.size() + " user(s)\n");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        System.out.println("GET /api/users/" + id + " - Fetching user");
        User user = userService.getUserById(id);
        System.out.println("✓ User found: " + user.getFullName() + "\n");
        return ResponseEntity.ok(user);
    }

    @PostMapping("/student")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        System.out.println("POST /api/users/student - Creating student");
        student.setRole(UserRole.STUDENT);
        Student saved = (Student) userService.createUser(student);
        System.out.println("✓ Student created: " + saved.getFullName() + " (ID: " + saved.getId() + ")\n");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/staff")
    public ResponseEntity<Staff> createStaff(@RequestBody Staff staff) {
        System.out.println("POST /api/users/staff - Creating staff");
        staff.setRole(UserRole.STAFF);
        Staff saved = (Staff) userService.createUser(staff);
        System.out.println("✓ Staff created: " + saved.getFullName() + " (ID: " + saved.getId() + ")\n");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/technician")
    public ResponseEntity<TechnicianSupportStaff> createTechnician(@RequestBody TechnicianSupportStaff technician) {
        System.out.println("POST /api/users/technician - Creating technician");
        technician.setRole(UserRole.TECHNICIAN);
        TechnicianSupportStaff saved = (TechnicianSupportStaff) userService.createUser(technician);
        System.out.println("✓ Technician created: " + saved.getFullName() + " (ID: " + saved.getId() + ")\n");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/admin")
    public ResponseEntity<Administrator> createAdmin(@RequestBody Administrator admin) {
        System.out.println("POST /api/users/admin - Creating administrator");
        admin.setRole(UserRole.ADMIN);
        Administrator saved = (Administrator) userService.createUser(admin);
        System.out.println("✓ Administrator created: " + saved.getFullName() + " (ID: " + saved.getId() + ")\n");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        System.out.println("PUT /api/users/" + id + " - Updating user");
        User updated = userService.updateUser(id, user);
        System.out.println("✓ User updated: " + updated.getFullName() + "\n");
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{adminId}/assign-role/{targetUserId}")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long adminId,
            @PathVariable Long targetUserId,
            @RequestParam String role) {

        System.out.println("ADMIN " + adminId + " assigning role " + role + " to user " + targetUserId);

        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        userService.assignRole(adminId, targetUserId, userRole);

        System.out.println("✓ Role assigned successfully\n");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/department")
    public ResponseEntity<Void> assignDepartment(@PathVariable Long id, @RequestParam Long departmentId) {
        System.out.println("PUT /api/users/" + id + "/department - Assigning department: " + departmentId);
        userService.assignDepartment(id, departmentId);
        System.out.println("✓ Department assigned successfully\n");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<String> getUserActivity(@PathVariable Long id) {
        System.out.println("GET /api/users/" + id + "/activity - Fetching activity log");
        String activity = userService.getUserActivity(id);
        System.out.println("✓ Activity log retrieved\n");
        return ResponseEntity.ok(activity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        System.out.println("DELETE /api/users/" + id + " - Deleting user");
        userService.deleteUser(id);
        System.out.println("✓ User deleted successfully\n");
        return ResponseEntity.noContent().build();
    }
}