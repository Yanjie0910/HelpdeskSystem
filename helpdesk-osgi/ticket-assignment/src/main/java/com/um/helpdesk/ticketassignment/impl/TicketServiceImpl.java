package com.um.helpdesk.ticketassignment.impl;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.TicketService;
import com.um.helpdesk.ticketassignment.repository.TicketRepository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    // Simple keyword-to-department mapping for routing
    private final Map<String, String> departmentKeywords;

    // Maximum reassignment allowed before requiring escalation
    private static final int MAX_REASSIGNMENT_COUNT = 3;

    public TicketServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.ticketRepository = new TicketRepository(entityManager);
        this.departmentKeywords = initializeDepartmentKeywords();
    }

    private Map<String, String> initializeDepartmentKeywords() {
        Map<String, String> keywords = new HashMap<>();
        // IT Department keywords
        keywords.put("network", "IT");
        keywords.put("computer", "IT");
        keywords.put("software", "IT");
        keywords.put("hardware", "IT");
        keywords.put("login", "IT");
        keywords.put("password", "IT");
        keywords.put("internet", "IT");
        keywords.put("wifi", "IT");
        keywords.put("email", "IT");

        // Facilities keywords
        keywords.put("room", "FACILITIES");
        keywords.put("building", "FACILITIES");
        keywords.put("maintenance", "FACILITIES");
        keywords.put("air conditioning", "FACILITIES");
        keywords.put("lighting", "FACILITIES");
        keywords.put("cleaning", "FACILITIES");
        keywords.put("projector", "FACILITIES");

        // Finance keywords
        keywords.put("payment", "FINANCE");
        keywords.put("tuition", "FINANCE");
        keywords.put("fees", "FINANCE");
        keywords.put("refund", "FINANCE");
        keywords.put("invoice", "FINANCE");

        // Academic keywords
        keywords.put("enrollment", "ACADEMIC");
        keywords.put("course", "ACADEMIC");
        keywords.put("grade", "ACADEMIC");
        keywords.put("exam", "ACADEMIC");
        keywords.put("registration", "ACADEMIC");

        return keywords;
    }

    // ========== BASIC TICKET OPERATIONS ==========

    @Override
    public Ticket createTicket(Ticket ticket) {
        if (ticket.getSubmittedAt() == null) {
            ticket.setSubmittedAt(LocalDateTime.now());
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.OPEN);
        }
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    @Override
    public List<Ticket> getTicketsByDepartment(Long departmentId) {
        return ticketRepository.findByDepartment(departmentId);
    }

    @Override
    public List<Ticket> getTicketsByTechnician(Long technicianId) {
        return ticketRepository.findByAssignedTo(technicianId);
    }

    @Override
    public List<Ticket> getTicketsBySubmitter(Long userId) {
        return ticketRepository.findBySubmittedBy(userId);
    }

    @Override
    public Ticket updateTicket(Long id, Ticket ticket) {
        Ticket existing = ticketRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
        ticket.setId(id);
        return ticketRepository.save(ticket);
    }

    @Override
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    // ========== FUNCTIONALITY 1: Automated Departmental Routing ==========

    @Override
    public Department routeTicketToDepartment(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }

        Department department = analyzeDepartmentFromTicket(ticket);
        if (department != null) {
            ticket.setAssignedDepartment(department);
            ticket.setStatus(TicketStatus.OPEN);
            ticketRepository.save(ticket);
            System.out.println(">>> Ticket #" + ticketId + " routed to department: " + department.getName());
        }

        return department;
    }

    @Override
    public Ticket autoRouteAndAssign(Long ticketId) {
        // First route to department
        Department department = routeTicketToDepartment(ticketId);

        if (department == null) {
            throw new RuntimeException("Could not determine appropriate department for ticket");
        }

        // Find least busy technician in that department
        TechnicianSupportStaff technician = findLeastBusyTechnician(department.getId());

        if (technician != null) {
            Ticket ticket = ticketRepository.findById(ticketId);
            ticket.setAssignedTo(technician);
            ticket.setAssignedAt(LocalDateTime.now());
            ticket.setStatus(TicketStatus.ASSIGNED);
            ticket = ticketRepository.save(ticket);
            System.out.println(">>> Ticket #" + ticketId + " auto-assigned to: " + technician.getFullName());
            return ticket;
        }

        return ticketRepository.findById(ticketId);
    }

    @Override
    public Department analyzeDepartmentFromTicket(Ticket ticket) {
        String searchText = (ticket.getTitle() + " " + ticket.getDescription() + " " +
                           (ticket.getCategory() != null ? ticket.getCategory() : "")).toLowerCase();

        // Match keywords to find appropriate department
        String matchedDeptCode = null;
        for (Map.Entry<String, String> entry : departmentKeywords.entrySet()) {
            if (searchText.contains(entry.getKey())) {
                matchedDeptCode = entry.getValue();
                break;
            }
        }

        // Find department by code
        if (matchedDeptCode != null) {
            List<Department> departments = entityManager
                .createQuery("SELECT d FROM Department d WHERE d.code = :code", Department.class)
                .setParameter("code", matchedDeptCode)
                .getResultList();

            if (!departments.isEmpty()) {
                return departments.get(0);
            }
        }

        // Default to IT department if no match
        List<Department> defaultDepts = entityManager
            .createQuery("SELECT d FROM Department d WHERE d.code = 'IT'", Department.class)
            .getResultList();

        return defaultDepts.isEmpty() ? null : defaultDepts.get(0);
    }

    // ========== FUNCTIONALITY 2: Ticket Claiming and Self-Assignment ==========

    @Override
    public List<Ticket> getUnassignedTicketsForDepartment(Long departmentId) {
        return ticketRepository.findUnassignedByDepartment(departmentId);
    }

    @Override
    public Ticket claimTicket(Long ticketId, Long technicianId) {
        Ticket ticket = ticketRepository.findById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }

        if (ticket.getAssignedTo() != null) {
            throw new RuntimeException("Ticket is already assigned to: " + ticket.getAssignedTo().getFullName());
        }

        TechnicianSupportStaff technician = entityManager.find(TechnicianSupportStaff.class, technicianId);
        if (technician == null) {
            throw new RuntimeException("Technician not found");
        }

        // Verify technician is in the same department as the ticket
        if (ticket.getAssignedDepartment() != null && technician.getDepartment() != null) {
            if (!ticket.getAssignedDepartment().getId().equals(technician.getDepartment().getId())) {
                throw new RuntimeException("Technician is not in the ticket's assigned department");
            }
        }

        ticket.setAssignedTo(technician);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket = ticketRepository.save(ticket);

        System.out.println(">>> Ticket #" + ticketId + " claimed by: " + technician.getFullName());
        return ticket;
    }

    @Override
    public List<Ticket> getClaimableTickets(Long technicianId) {
        TechnicianSupportStaff technician = entityManager.find(TechnicianSupportStaff.class, technicianId);
        if (technician == null || technician.getDepartment() == null) {
            return Collections.emptyList();
        }

        return getUnassignedTicketsForDepartment(technician.getDepartment().getId());
    }

    // ========== FUNCTIONALITY 3: Internal Re-assignment and Chaining ==========

    @Override
    public Ticket reassignTicketInternally(Long ticketId, Long newTechnicianId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }

        if (!canReassign(ticketId)) {
            throw new RuntimeException("Ticket has reached maximum reassignment limit");
        }

        TechnicianSupportStaff currentTechnician = ticket.getAssignedTo();
        TechnicianSupportStaff newTechnician = entityManager.find(TechnicianSupportStaff.class, newTechnicianId);

        if (newTechnician == null) {
            throw new RuntimeException("New technician not found");
        }

        // Verify both technicians are in the same department
        if (currentTechnician != null && currentTechnician.getDepartment() != null &&
            newTechnician.getDepartment() != null) {
            if (!currentTechnician.getDepartment().getId().equals(newTechnician.getDepartment().getId())) {
                throw new RuntimeException("Cannot reassign: technicians are in different departments. Use transfer instead.");
            }
        }

        // Track previous assignee
        ticket.setPreviousAssignee(currentTechnician);
        ticket.setAssignedTo(newTechnician);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setReassignmentCount(ticket.getReassignmentCount() + 1);
        ticket.setStatus(TicketStatus.ASSIGNED);

        ticket = ticketRepository.save(ticket);

        System.out.println(">>> Ticket #" + ticketId + " reassigned from " +
            (currentTechnician != null ? currentTechnician.getFullName() : "unassigned") +
            " to " + newTechnician.getFullName() + ". Reason: " + reason);

        return ticket;
    }

    @Override
    public List<TechnicianSupportStaff> getAssignmentHistory(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId);
        if (ticket == null) {
            return Collections.emptyList();
        }

        List<TechnicianSupportStaff> history = new ArrayList<>();
        if (ticket.getPreviousAssignee() != null) {
            history.add(ticket.getPreviousAssignee());
        }
        if (ticket.getAssignedTo() != null) {
            history.add(ticket.getAssignedTo());
        }

        return history;
    }

    @Override
    public boolean canReassign(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId);
        return ticket != null && ticket.getReassignmentCount() < MAX_REASSIGNMENT_COUNT;
    }

    // ========== FUNCTIONALITY 4: Inter-Departmental Transfer ==========

    @Override
    public Ticket transferTicketToDepartment(Long ticketId, Long newDepartmentId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }

        Department newDepartment = entityManager.find(Department.class, newDepartmentId);
        if (newDepartment == null) {
            throw new RuntimeException("Department not found");
        }

        Department oldDepartment = ticket.getAssignedDepartment();

        // Check if already assigned to this department
        if (oldDepartment != null && oldDepartment.getId().equals(newDepartmentId)) {
            throw new RuntimeException("Ticket is already assigned to this department");
        }

        TechnicianSupportStaff oldTechnician = ticket.getAssignedTo();

        // Transfer to new department
        ticket.setAssignedDepartment(newDepartment);
        ticket.setPreviousAssignee(oldTechnician);
        ticket.setAssignedTo(null);  // Unassign technician
        ticket.setStatus(TicketStatus.OPEN);  // Back to open status for new department
        ticket.setReassignmentCount(ticket.getReassignmentCount() + 1);

        ticket = ticketRepository.save(ticket);

        System.out.println(">>> Ticket #" + ticketId + " transferred from " +
            (oldDepartment != null ? oldDepartment.getName() : "unassigned") +
            " to " + newDepartment.getName() + ". Reason: " + reason);

        return ticket;
    }

    @Override
    public Ticket transferAndAssign(Long ticketId, Long newDepartmentId, Long newTechnicianId, String reason) {
        // First transfer to new department
        Ticket ticket = transferTicketToDepartment(ticketId, newDepartmentId, reason);

        // Then assign to specific technician
        TechnicianSupportStaff newTechnician = entityManager.find(TechnicianSupportStaff.class, newTechnicianId);
        if (newTechnician == null) {
            throw new RuntimeException("Technician not found");
        }

        // Verify technician is in the new department
        if (newTechnician.getDepartment() == null ||
            !newTechnician.getDepartment().getId().equals(newDepartmentId)) {
            throw new RuntimeException("Technician is not in the target department");
        }

        ticket.setAssignedTo(newTechnician);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.ASSIGNED);

        ticket = ticketRepository.save(ticket);

        System.out.println(">>> Ticket #" + ticketId + " assigned to " + newTechnician.getFullName() +
            " in " + newTechnician.getDepartment().getName());

        return ticket;
    }

    @Override
    public List<Ticket> getTransferredTickets(Long fromDepartmentId, Long toDepartmentId) {
        // This is a simplified implementation
        // In a real system, you'd maintain a transfer history table
        return ticketRepository.findByDepartment(toDepartmentId).stream()
            .filter(t -> t.getPreviousAssignee() != null)
            .filter(t -> t.getPreviousAssignee().getDepartment() != null)
            .filter(t -> t.getPreviousAssignee().getDepartment().getId().equals(fromDepartmentId))
            .collect(Collectors.toList());
    }

    // ========== ASSIGNMENT ANALYTICS ==========

    @Override
    public int getTechnicianWorkload(Long technicianId) {
        return (int) ticketRepository.countByTechnician(technicianId);
    }

    @Override
    public TechnicianSupportStaff findLeastBusyTechnician(Long departmentId) {
        List<TechnicianSupportStaff> technicians = entityManager
            .createQuery("SELECT t FROM TechnicianSupportStaff t WHERE t.department.id = :deptId AND t.active = true",
                        TechnicianSupportStaff.class)
            .setParameter("deptId", departmentId)
            .getResultList();

        if (technicians.isEmpty()) {
            return null;
        }

        // Find technician with least workload
        TechnicianSupportStaff leastBusy = technicians.get(0);
        int minWorkload = getTechnicianWorkload(leastBusy.getId());

        for (TechnicianSupportStaff tech : technicians) {
            int workload = getTechnicianWorkload(tech.getId());
            if (workload < minWorkload) {
                minWorkload = workload;
                leastBusy = tech;
            }
        }

        return leastBusy;
    }

    @Override
    public int getDepartmentWorkload(Long departmentId) {
        return (int) ticketRepository.countByDepartment(departmentId);
    }
}
