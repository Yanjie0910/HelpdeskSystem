package com.um.helpdesk.usermgmt.impl;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.UserManagementService;
import com.um.helpdesk.usermgmt.repository.DepartmentRepository;
import com.um.helpdesk.usermgmt.repository.UserRepository;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class UserManagementServiceImpl implements UserManagementService {

    // ===== JPA mode fields =====
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // ===== In-memory mode fields =====
    private final Map<Long, User> memUsers = new LinkedHashMap<>();
    private final Map<Long, Department> memDepartments = new LinkedHashMap<>();
    private final AtomicLong memUserId = new AtomicLong(1);
    private final AtomicLong memDeptId = new AtomicLong(1);

    // ---------------- Constructors ----------------

    /**
     * ✅ In-memory constructor (default for Karaf when no JPA provider)
     */
    public UserManagementServiceImpl() {
        this.entityManager = null;
        this.userRepository = null;
        this.departmentRepository = null;
        System.out.println("[UserManagement] Running in IN-MEMORY mode (no JPA).");
    }

    /**
     * ✅ JPA constructor (only use when Karaf has a real JPA provider configured)
     */
    public UserManagementServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.userRepository = new UserRepository(entityManager);
        this.departmentRepository = new DepartmentRepository(entityManager);
        System.out.println("[UserManagement] Running in JPA mode.");
    }

    private boolean isJpaMode() {
        return entityManager != null;
    }

    // ---------------- Helper: In-memory ID handling ----------------

    private Long nextUserId() {
        return memUserId.getAndIncrement();
    }

    private Long nextDeptId() {
        return memDeptId.getAndIncrement();
    }

    // ==================== FUNCTIONALITY 1: MANAGE USER PROFILES ====================

    @Override
    public User createUser(User user) {
        System.out.println("[UserManagement] Creating user: " + (user != null ? user.getEmail() : "null"));

        if (user == null) {
            throw new RuntimeException("User cannot be null");
        }

        if (isJpaMode()) {
            return userRepository.save(user);
        }

        // In-memory: enforce unique email
        for (User u : memUsers.values()) {
            if (u.getEmail() != null && u.getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new RuntimeException("Email already exists: " + user.getEmail());
            }
        }

        if (user.getId() == null) user.setId(nextUserId());
        if (user.getRole() == null) user.setRole(UserRole.STUDENT);
        memUsers.put(user.getId(), user);

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        System.out.println("[UserManagement] Fetching all users");

        if (isJpaMode()) {
            return userRepository.findAll();
        }

        return new ArrayList<>(memUsers.values());
    }

    @Override
    public User getUserById(Long id) {
        System.out.println("[UserManagement] Fetching user with ID: " + id);

        if (id == null) throw new RuntimeException("User id cannot be null");

        if (isJpaMode()) {
            User user = userRepository.findById(id);
            if (user == null) throw new RuntimeException("User not found with id: " + id);
            return user;
        }

        User user = memUsers.get(id);
        if (user == null) throw new RuntimeException("User not found with id: " + id);
        return user;
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        System.out.println("[UserManagement] Updating user ID: " + id);

        if (id == null) throw new RuntimeException("User id cannot be null");
        if (userDetails == null) throw new RuntimeException("User details cannot be null");

        User user = getUserById(id);

        // Update fields
        if (userDetails.getFullName() != null) user.setFullName(userDetails.getFullName());
        if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
        if (userDetails.getPhoneNumber() != null) user.setPhoneNumber(userDetails.getPhoneNumber());

        if (isJpaMode()) {
            return userRepository.save(user);
        }

        memUsers.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        System.out.println("[UserManagement] Deleting user ID: " + id);

        if (id == null) throw new RuntimeException("User id cannot be null");

        if (isJpaMode()) {
            if (!userRepository.exists(id)) {
                throw new RuntimeException("User not found with id: " + id);
            }
            userRepository.deleteById(id);
            return;
        }

        if (!memUsers.containsKey(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        memUsers.remove(id);
    }

    // ==================== FUNCTIONALITY 2: MANAGE ROLES AND PERMISSIONS ====================

    @Override
    public void assignRole(Long adminId, Long targetUserId, UserRole role) {
        System.out.println("[UserManagement] Admin " + adminId + " assigning role " + role + " to user " + targetUserId);

        if (adminId == null || targetUserId == null) {
            throw new RuntimeException("adminId and targetUserId cannot be null");
        }
        if (role == null) throw new RuntimeException("role cannot be null");

        User admin = getUserById(adminId);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied: Only ADMIN can assign roles. User ID " + adminId + " is not an admin.");
        }

        User targetUser = getUserById(targetUserId);
        targetUser.setRole(role);

        if (isJpaMode()) {
            userRepository.save(targetUser);
        } else {
            memUsers.put(targetUser.getId(), targetUser);
        }

        System.out.println("[UserManagement] ✓ Role assigned successfully");
    }

    // ==================== FUNCTIONALITY 3: MANAGE DEPARTMENT ASSIGNMENTS ====================

    @Override
    public void assignDepartment(Long userId, Long departmentId) {
        System.out.println("[UserManagement] Assigning user " + userId + " to department " + departmentId);

        if (userId == null || departmentId == null) {
            throw new RuntimeException("userId and departmentId cannot be null");
        }

        User user = getUserById(userId);

        Department department;
        if (isJpaMode()) {
            department = departmentRepository.findById(departmentId);
        } else {
            department = memDepartments.get(departmentId);
        }

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

        if (isJpaMode()) {
            userRepository.save(user);
        } else {
            memUsers.put(user.getId(), user);
        }

        System.out.println("[UserManagement] ✓ Department assigned successfully");
    }

    // ==================== FUNCTIONALITY 4: VIEW USER ACTIVITY AND AUDIT LOGS ====================

    @Override
    public String getUserActivity(Long userId) {
        System.out.println("[UserManagement] Fetching activity for user ID: " + userId);

        if (userId == null) throw new RuntimeException("userId cannot be null");

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

    public Department createDepartment(Department d) {
        if (d == null) throw new RuntimeException("Department cannot be null");

        if (isJpaMode()) {
            return d;
        }

        if (d.getId() == null) d.setId(nextDeptId());
        memDepartments.put(d.getId(), d);
        return d;
    }
}
