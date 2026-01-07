package com.um.helpdesk.usermgmt.repository;

import com.um.helpdesk.entity.User;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Save (create or update) a user
     */
    public User save(User user) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            
            if (user.getId() == null) {
                // New user - persist
                entityManager.persist(user);
            } else {
                // Existing user - merge
                user = entityManager.merge(user);
            }
            
            transaction.commit();
            return user;
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    /**
     * Find all users
     */
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }

    /**
     * Find user by ID
     */
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    /**
     * Check if user exists
     */
    public boolean exists(Long id) {
        return findById(id) != null;
    }

    /**
     * Delete user by ID
     */
    public void deleteById(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            
            User user = entityManager.find(User.class, id);
            if (user != null) {
                entityManager.remove(user);
            }
            
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }
}
