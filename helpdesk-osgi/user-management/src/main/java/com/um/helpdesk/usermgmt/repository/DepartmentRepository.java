package com.um.helpdesk.usermgmt.repository;

import com.um.helpdesk.entity.Department;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class DepartmentRepository {

    private final EntityManager entityManager;

    public DepartmentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Save (create or update) a department
     */
    public Department save(Department department) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            
            if (department.getId() == null) {
                entityManager.persist(department);
            } else {
                department = entityManager.merge(department);
            }
            
            transaction.commit();
            return department;
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save department: " + e.getMessage(), e);
        }
    }

    /**
     * Find all departments
     */
    public List<Department> findAll() {
        return entityManager.createQuery("SELECT d FROM Department d", Department.class)
                .getResultList();
    }

    /**
     * Find department by ID
     */
    public Department findById(Long id) {
        return entityManager.find(Department.class, id);
    }

    /**
     * Check if department exists
     */
    public boolean exists(Long id) {
        return findById(id) != null;
    }

    /**
     * Delete department by ID
     */
    public void deleteById(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            
            Department department = entityManager.find(Department.class, id);
            if (department != null) {
                entityManager.remove(department);
            }
            
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete department: " + e.getMessage(), e);
        }
    }
}
