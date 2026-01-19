package com.um.helpdesk.service;

import com.um.helpdesk.entity.Department;
import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TechnicianSupportStaff;
import com.um.helpdesk.entity.TicketStatus;
import java.util.List;

/**
 * Service interface for Ticket Assignment Module
 * Implements 4 core functionalities as per CBSE assignment requirements
 */
public interface TicketService {

    // ========== BASIC TICKET OPERATIONS ==========

    Ticket createTicket(Ticket ticket);
    Ticket getTicketById(Long id);
    List<Ticket> getAllTickets();
    List<Ticket> getTicketsByStatus(TicketStatus status);
    List<Ticket> getTicketsByDepartment(Long departmentId);
    List<Ticket> getTicketsByTechnician(Long technicianId);
    List<Ticket> getTicketsBySubmitter(Long userId);
    Ticket updateTicket(Long id, Ticket ticket);
    void deleteTicket(Long id);

    // ========== FUNCTIONALITY 1: Automated Departmental Routing ==========

    /**
     * Automatically routes a ticket to the appropriate department based on:
     * - Ticket category
     * - Keywords in title/description
     * - Department specializations
     */
    Department routeTicketToDepartment(Long ticketId);

    /**
     * Routes ticket and assigns to available technician in that department
     */
    Ticket autoRouteAndAssign(Long ticketId);

    /**
     * Analyzes ticket content to determine best department
     */
    Department analyzeDepartmentFromTicket(Ticket ticket);

    // ========== FUNCTIONALITY 2: Ticket Claiming and Self-Assignment ==========

    /**
     * Get list of unassigned tickets in technician's department
     */
    List<Ticket> getUnassignedTicketsForDepartment(Long departmentId);

    /**
     * Allows technician to claim/self-assign an unassigned ticket
     */
    Ticket claimTicket(Long ticketId, Long technicianId);

    /**
     * Get tickets available for claiming by a specific technician
     */
    List<Ticket> getClaimableTickets(Long technicianId);

    // ========== FUNCTIONALITY 3: Internal Re-assignment and Chaining ==========

    /**
     * Reassign ticket to another technician within the SAME department
     * Tracks previous assignee and maintains assignment history
     */
    Ticket reassignTicketInternally(Long ticketId, Long newTechnicianId, String reason);

    /**
     * Get assignment history/chain for a ticket
     */
    List<TechnicianSupportStaff> getAssignmentHistory(Long ticketId);

    /**
     * Check if ticket can be reassigned (e.g., not exceeded reassignment limit)
     */
    boolean canReassign(Long ticketId);

    // ========== FUNCTIONALITY 4: Inter-Departmental Transfer ==========

    /**
     * Transfer ticket to a DIFFERENT department
     * Unassigns current technician and routes to new department
     */
    Ticket transferTicketToDepartment(Long ticketId, Long newDepartmentId, String reason);

    /**
     * Transfer and immediately assign to specific technician in new department
     */
    Ticket transferAndAssign(Long ticketId, Long newDepartmentId, Long newTechnicianId, String reason);

    /**
     * Get tickets transferred from one department to another
     */
    List<Ticket> getTransferredTickets(Long fromDepartmentId, Long toDepartmentId);

    // ========== ASSIGNMENT ANALYTICS ==========

    /**
     * Get workload statistics for a technician
     */
    int getTechnicianWorkload(Long technicianId);

    /**
     * Find technician with least workload in department for load balancing
     */
    TechnicianSupportStaff findLeastBusyTechnician(Long departmentId);

    /**
     * Get department workload statistics
     */
    int getDepartmentWorkload(Long departmentId);
}
