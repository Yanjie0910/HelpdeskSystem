package com.um.helpdesk.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.UserRepository;
import com.um.helpdesk.repository.DepartmentRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public UserServiceImpl(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setFullName(userDetails.getFullName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setEmail(userDetails.getEmail());
        
        return userRepository.save(user);
    }

    @Override
    public void assignRole(Long adminId, Long targetUserId, UserRole role) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("No record found for admin ID: " + adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied: Only ADMIN can assign roles. User ID " + adminId + " is not an admin.");
        }

        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new RuntimeException("No record found for user ID: " + targetUserId));

        targetUser.setRole(role);
        userRepository.save(targetUser);
    }


    @Override
    public void assignDepartment(Long userId, Long departmentId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        
        if (user instanceof Staff) {
            ((Staff) user).setDepartment(department);
        } else if (user instanceof TechnicianSupportStaff) {
            ((TechnicianSupportStaff) user).setDepartment(department);
        } else {
            throw new RuntimeException("Only Staff and Technician can be assigned to departments");
        }
        
        userRepository.save(user);
    }

    @Override
    public String getUserActivity(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        StringBuilder activity = new StringBuilder();
        activity.append("=== USER ACTIVITY LOG ===\n");
        activity.append("User ID: ").append(user.getId()).append("\n");
        activity.append("Email: ").append(user.getEmail()).append("\n");
        activity.append("Full Name: ").append(user.getFullName()).append("\n");
        activity.append("Role: ").append(user.getRole()).append("\n");
        activity.append("Active: ").append(user.isActive() ? "Yes" : "No").append("\n");
        activity.append("Created At: ").append(user.getCreatedAt()).append("\n");
        activity.append("Last Updated: ").append(user.getUpdatedAt()).append("\n");
        activity.append("\n--- RECENT ACTIVITY ---\n");
        activity.append("- Account created on: ").append(user.getCreatedAt()).append("\n");
        activity.append("- Last profile update: ").append(user.getUpdatedAt()).append("\n");
        activity.append("- Current status: ").append(user.isActive() ? "Active" : "Inactive").append("\n");
        
        return activity.toString();
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}