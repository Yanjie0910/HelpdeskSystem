package com.um.helpdesk.service;

import com.um.helpdesk.entity.User;
import com.um.helpdesk.entity.UserRole;
import java.util.List;


public interface UserManagementService {
    
    // ==================== FUNCTIONALITY 1: MANAGE USER PROFILES ====================
    
    /**
     * Create a new user account (UC1)
     * @param user The user entity to create
     * @return The created user with generated ID
     */
    User createUser(User user);
    
    /**
     * Retrieve all users in the system (UC1)
     * @return List of all users
     */
    List<User> getAllUsers();
    
    /**
     * Retrieve a specific user by ID (UC1)
     * @param id The user ID
     * @return The user entity
     * @throws RuntimeException if user not found
     */
    User getUserById(Long id);
    
    /**
     * Update user profile information (UC1)
     * @param id The user ID to update
     * @param userDetails The updated user information
     * @return The updated user entity
     * @throws RuntimeException if user not found
     */
    User updateUser(Long id, User userDetails);
    
    /**
     * Delete a user account (UC1)
     * @param id The user ID to delete
     * @throws RuntimeException if user not found
     */
    void deleteUser(Long id);
    
    // ==================== FUNCTIONALITY 2: MANAGE ROLES AND PERMISSIONS ====================
    
    /**
     * Assign a role to a user (UC2)
     * Requires admin privileges
     * 
     * @param adminId The ID of the admin performing the operation
     * @param targetUserId The ID of the user to assign role to
     * @param role The new role to assign
     * @throws RuntimeException if admin check fails or user not found
     */
    void assignRole(Long adminId, Long targetUserId, UserRole role);
    
    // ==================== FUNCTIONALITY 3: MANAGE DEPARTMENT ASSIGNMENTS ====================
    
    /**
     * Assign a department to a staff or technician (UC3)
     * Only Staff and TechnicianSupportStaff can be assigned to departments
     * 
     * @param userId The user ID (must be Staff or Technician)
     * @param departmentId The department ID to assign
     * @throws RuntimeException if user is not Staff/Technician or department not found
     */
    void assignDepartment(Long userId, Long departmentId);
    
    // ==================== FUNCTIONALITY 4: VIEW USER ACTIVITY AND AUDIT LOGS ====================
    
    /**
     * Retrieve user activity log (UC4)
     * Returns formatted activity information including:
     * - User profile details
     * - Account creation date
     * - Last update timestamp
     * - Current active status
     * 
     * @param userId The user ID
     * @return Formatted activity log string
     * @throws RuntimeException if user not found
     */
    String getUserActivity(Long userId);
}
