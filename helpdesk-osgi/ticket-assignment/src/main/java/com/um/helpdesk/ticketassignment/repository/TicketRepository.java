package com.um.helpdesk.ticketassignment.repository;

import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketStatus;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class TicketRepository {

    private final EntityManager entityManager;

    public TicketRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Save (create or update) a ticket
     */
    public Ticket save(Ticket ticket) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            if (ticket.getId() == null) {
                entityManager.persist(ticket);
            } else {
                ticket = entityManager.merge(ticket);
            }

            transaction.commit();
            return ticket;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save ticket: " + e.getMessage(), e);
        }
    }

    /**
     * Find all tickets
     */
    public List<Ticket> findAll() {
        return entityManager.createQuery("SELECT t FROM Ticket t ORDER BY t.createdAt DESC", Ticket.class)
                .getResultList();
    }

    /**
     * Find ticket by ID
     */
    public Ticket findById(Long id) {
        return entityManager.find(Ticket.class, id);
    }

    /**
     * Find tickets by status
     */
    public List<Ticket> findByStatus(TicketStatus status) {
        return entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt DESC",
            Ticket.class)
            .setParameter("status", status)
            .getResultList();
    }

    /**
     * Find tickets by department
     */
    public List<Ticket> findByDepartment(Long departmentId) {
        return entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.assignedDepartment.id = :deptId ORDER BY t.createdAt DESC",
            Ticket.class)
            .setParameter("deptId", departmentId)
            .getResultList();
    }

    /**
     * Find tickets assigned to a technician
     */
    public List<Ticket> findByAssignedTo(Long technicianId) {
        return entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.assignedTo.id = :techId ORDER BY t.createdAt DESC",
            Ticket.class)
            .setParameter("techId", technicianId)
            .getResultList();
    }

    /**
     * Find tickets submitted by a user
     */
    public List<Ticket> findBySubmittedBy(Long userId) {
        return entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.submittedBy.id = :userId ORDER BY t.createdAt DESC",
            Ticket.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    /**
     * Find unassigned tickets in a department
     */
    public List<Ticket> findUnassignedByDepartment(Long departmentId) {
        return entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.assignedDepartment.id = :deptId " +
            "AND t.assignedTo IS NULL AND t.status = :status ORDER BY t.priority DESC, t.createdAt ASC",
            Ticket.class)
            .setParameter("deptId", departmentId)
            .setParameter("status", TicketStatus.OPEN)
            .getResultList();
    }

    /**
     * Count tickets by technician (for workload calculation)
     */
    public long countByTechnician(Long technicianId) {
        return entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :techId " +
            "AND t.status NOT IN (:excludedStatuses)",
            Long.class)
            .setParameter("techId", technicianId)
            .setParameter("excludedStatuses", List.of(TicketStatus.CLOSED, TicketStatus.RESOLVED))
            .getSingleResult();
    }

    /**
     * Count tickets by department
     */
    public long countByDepartment(Long departmentId) {
        return entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.assignedDepartment.id = :deptId " +
            "AND t.status NOT IN (:excludedStatuses)",
            Long.class)
            .setParameter("deptId", departmentId)
            .setParameter("excludedStatuses", List.of(TicketStatus.CLOSED, TicketStatus.RESOLVED))
            .getSingleResult();
    }

    /**
     * Delete ticket by ID
     */
    public void deleteById(Long id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Ticket ticket = entityManager.find(Ticket.class, id);
            if (ticket != null) {
                entityManager.remove(ticket);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete ticket: " + e.getMessage(), e);
        }
    }
}
