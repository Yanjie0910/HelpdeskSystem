package com.um.helpdesk.service;

import com.um.helpdesk.entity.User;
import com.um.helpdesk.entity.UserRole;
import java.util.List;

public interface UserService {
    User createUser(User user);
    List<User> getAllUsers();
    User getUserById(Long id);
    User updateUser(Long id, User userDetails);
    void assignRole(Long adminId, Long targetUserId, UserRole role) ;
    void assignDepartment(Long userId, Long departmentId);
    String getUserActivity(Long userId);
    void deleteUser(Long id);
}