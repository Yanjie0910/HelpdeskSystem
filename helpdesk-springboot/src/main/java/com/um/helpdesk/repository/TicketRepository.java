package com.um.helpdesk.repository;

import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByAssignedDepartmentId(Long departmentId);

    List<Ticket> findByAssignedToId(Long technicianId);

    List<Ticket> findBySubmittedById(Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.assignedDepartment.id = :deptId " +
           "AND t.assignedTo IS NULL AND t.status = 'OPEN' " +
           "ORDER BY t.priority DESC, t.createdAt ASC")
    List<Ticket> findUnassignedByDepartment(@Param("deptId") Long departmentId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :techId " +
           "AND t.status NOT IN ('CLOSED', 'RESOLVED')")
    long countActiveTicketsByTechnician(@Param("techId") Long technicianId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedDepartment.id = :deptId " +
           "AND t.status NOT IN ('CLOSED', 'RESOLVED')")
    long countActiveTicketsByDepartment(@Param("deptId") Long departmentId);
}
