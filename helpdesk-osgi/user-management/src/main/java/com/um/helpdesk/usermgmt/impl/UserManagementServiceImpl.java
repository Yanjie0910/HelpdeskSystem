package com.um.helpdesk.usermgmt.impl;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserManagementService;
import com.um.helpdesk.usermgmt.repository.UserRepository;
import com.um.helpdesk.usermgmt.repository.DepartmentRepository;

import javax.persistence.EntityManager;
import java.util.List;

public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EntityManager entityManager;

    public UserManagementServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.userRepository = new UserRepository(entityManager);
        this.departmentRepository = new DepartmentRepository(entityManager);
    }

    // ==================== FUNCTIONALITY 1: MANAGE USER PROFILES ====================

    @Override
    public User createUser(User user) {
        System.out.println("[UserManagement] Creating user: " + user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        System.out.println("[UserManagement] Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        System.out.println("[UserManagement] Fetching user with ID: " + id);
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user;
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        System.out.println("[UserManagement] Updating user ID: " + id);
        
        User user = getUserById(id);
        
        // Update fields
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        System.out.println("[UserManagement] Deleting user ID: " + id);
        
        if (!userRepository.exists(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
    }

    // ==================== FUNCTIONALITY 2: MANAGE ROLES AND PERMISSIONS ====================

    @Override
    public void assignRole(Long adminId, Long targetUserId, UserRole role) {
        System.out.println("[UserManagement] Admin " + adminId + " assigning role " + role + " to user " + targetUserId);
        
        // Verify admin privileges
        User admin = getUserById(adminId);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied: Only ADMIN can assign roles. User ID " + adminId + " is not an admin.");
        }
        
        // Get target user
        User targetUser = getUserById(targetUserId);
        
        // Assign new role
        targetUser.setRole(role);
        userRepository.save(targetUser);
        
        System.out.println("[UserManagement] ✓ Role assigned successfully");
    }

    // ==================== FUNCTIONALITY 3: MANAGE DEPARTMENT ASSIGNMENTS ====================

    @Override
    public void assignDepartment(Long userId, Long departmentId) {
        System.out.println("[UserManagement] Assigning user " + userId + " to department " + departmentId);
        
        User user = getUserById(userId);
        Department department = departmentRepository.findById(departmentId);
        
        if (department == null) {
            throw new RuntimeException("Department not found with id: " + departmentId);
        }
        
        // Only Staff and Technician can be assigned to departments
        if (user instanceof Staff) {
            ((Staff) user).setDepartment(department);
        } else if (user instanceof TechnicianSupportStaff) {
            ((TechnicianSupportStaff) user).setDepartment(department);
        } else {
            throw new RuntimeException("Only Staff and Technician can be assigned to departments");
        }
        
        userRepository.save(user);
        System.out.println("[UserManagement] ✓ Department assigned successfully");
    }

    // ==================== FUNCTIONALITY 4: VIEW USER ACTIVITY AND AUDIT LOGS ====================

    @Override
    public String getUserActivity(Long userId) {
        System.out.println("[UserManagement] Fetching activity for user ID: " + userId);
        
        User user = getUserById(userId);
        
        StringBuilder activity = new StringBuilder();
        activity.append("╔════════════════════════════════════════════════════════╗\n");
        activity.append("║              USER ACTIVITY LOG                         ║\n");
        activity.append("╚════════════════════════════════════════════════════════╝\n\n");
        activity.append("User ID:      ").append(user.getId()).append("\n");
        activity.append("Email:        ").append(user.getEmail()).append("\n");
        activity.append("Full Name:    ").append(user.getFullName()).append("\n");
        activity.append("Role:         ").append(user.getRole()).append("\n");
        activity.append("Active:       ").append(user.isActive() ? "Yes" : "No").append("\n");
        activity.append("Created At:   ").append(user.getCreatedAt()).append("\n");
        activity.append("Last Updated: ").append(user.getUpdatedAt()).append("\n");
        activity.append("\n--- RECENT ACTIVITY ---\n");
        activity.append("• Account created on: ").append(user.getCreatedAt()).append("\n");
        activity.append("• Last profile update: ").append(user.getUpdatedAt()).append("\n");
        activity.append("• Current status: ").append(user.isActive() ? "Active" : "Inactive").append("\n");
        
        return activity.toString();
    }
}
