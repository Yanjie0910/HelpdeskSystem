package com.um.helpdesk.controller;

import com.um.helpdesk.entity.Department;
import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TechnicianSupportStaff;
import com.um.helpdesk.entity.TicketStatus;
import com.um.helpdesk.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ========== BASIC TICKET OPERATIONS ==========

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        System.out.println("GET /api/tickets - Fetching all tickets");
        List<Ticket> tickets = ticketService.getAllTickets();
        System.out.println("✓ Found " + tickets.size() + " ticket(s)\n");
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        System.out.println("GET /api/tickets/" + id + " - Fetching ticket");
        Ticket ticket = ticketService.getTicketById(id);
        System.out.println("✓ Ticket found: " + ticket.getTitle() + "\n");
        return ResponseEntity.ok(ticket);
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        System.out.println("POST /api/tickets - Creating ticket");
        Ticket saved = ticketService.createTicket(ticket);
        System.out.println("✓ Ticket created: " + saved.getTitle() + " (ID: " + saved.getId() + ")\n");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        System.out.println("PUT /api/tickets/" + id + " - Updating ticket");
        Ticket updated = ticketService.updateTicket(id, ticket);
        System.out.println("✓ Ticket updated\n");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        System.out.println("DELETE /api/tickets/" + id + " - Deleting ticket");
        ticketService.deleteTicket(id);
        System.out.println("✓ Ticket deleted\n");
        return ResponseEntity.noContent().build();
    }

    // ========== FUNCTIONALITY 1: Automated Departmental Routing ==========

    @PostMapping("/{id}/route")
    public ResponseEntity<Department> routeTicket(@PathVariable Long id) {
        System.out.println("POST /api/tickets/" + id + "/route - Routing ticket to department");
        Department department = ticketService.routeTicketToDepartment(id);
        System.out.println("✓ Ticket routed to: " + department.getName() + "\n");
        return ResponseEntity.ok(department);
    }

    @PostMapping("/{id}/auto-assign")
    public ResponseEntity<Ticket> autoRouteAndAssign(@PathVariable Long id) {
        System.out.println("POST /api/tickets/" + id + "/auto-assign - Auto-routing and assigning ticket");
        Ticket ticket = ticketService.autoRouteAndAssign(id);
        System.out.println("✓ Ticket auto-assigned\n");
        return ResponseEntity.ok(ticket);
    }

    // ========== FUNCTIONALITY 2: Ticket Claiming and Self-Assignment ==========

    @GetMapping("/department/{deptId}/unassigned")
    public ResponseEntity<List<Ticket>> getUnassignedTickets(@PathVariable Long deptId) {
        System.out.println("GET /api/tickets/department/" + deptId + "/unassigned - Fetching unassigned tickets");
        List<Ticket> tickets = ticketService.getUnassignedTicketsForDepartment(deptId);
        System.out.println("✓ Found " + tickets.size() + " unassigned ticket(s)\n");
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/{ticketId}/claim/{technicianId}")
    public ResponseEntity<Ticket> claimTicket(@PathVariable Long ticketId, @PathVariable Long technicianId) {
        System.out.println("POST /api/tickets/" + ticketId + "/claim/" + technicianId + " - Technician claiming ticket");
        Ticket ticket = ticketService.claimTicket(ticketId, technicianId);
        System.out.println("✓ Ticket claimed\n");
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/technician/{techId}/claimable")
    public ResponseEntity<List<Ticket>> getClaimableTickets(@PathVariable Long techId) {
        System.out.println("GET /api/tickets/technician/" + techId + "/claimable - Fetching claimable tickets");
        List<Ticket> tickets = ticketService.getClaimableTickets(techId);
        System.out.println("✓ Found " + tickets.size() + " claimable ticket(s)\n");
        return ResponseEntity.ok(tickets);
    }

    // ========== FUNCTIONALITY 3: Internal Re-assignment and Chaining ==========

    @PostMapping("/{ticketId}/reassign/{newTechId}")
    public ResponseEntity<Ticket> reassignTicket(@PathVariable Long ticketId,
                                                   @PathVariable Long newTechId,
                                                   @RequestParam String reason) {
        System.out.println("POST /api/tickets/" + ticketId + "/reassign/" + newTechId + " - Reassigning ticket");
        Ticket ticket = ticketService.reassignTicketInternally(ticketId, newTechId, reason);
        System.out.println("✓ Ticket reassigned\n");
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{ticketId}/history")
    public ResponseEntity<List<TechnicianSupportStaff>> getAssignmentHistory(@PathVariable Long ticketId) {
        System.out.println("GET /api/tickets/" + ticketId + "/history - Fetching assignment history");
        List<TechnicianSupportStaff> history = ticketService.getAssignmentHistory(ticketId);
        System.out.println("✓ Found " + history.size() + " assignment(s) in history\n");
        return ResponseEntity.ok(history);
    }

    // ========== FUNCTIONALITY 4: Inter-Departmental Transfer ==========

    @PostMapping("/{ticketId}/transfer/{newDeptId}")
    public ResponseEntity<Ticket> transferTicket(@PathVariable Long ticketId,
                                                   @PathVariable Long newDeptId,
                                                   @RequestParam String reason) {
        System.out.println("POST /api/tickets/" + ticketId + "/transfer/" + newDeptId + " - Transferring ticket");
        Ticket ticket = ticketService.transferTicketToDepartment(ticketId, newDeptId, reason);
        System.out.println("✓ Ticket transferred\n");
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{ticketId}/transfer/{newDeptId}/assign/{newTechId}")
    public ResponseEntity<Ticket> transferAndAssign(@PathVariable Long ticketId,
                                                      @PathVariable Long newDeptId,
                                                      @PathVariable Long newTechId,
                                                      @RequestParam String reason) {
        System.out.println("POST /api/tickets/" + ticketId + "/transfer/" + newDeptId + "/assign/" + newTechId);
        Ticket ticket = ticketService.transferAndAssign(ticketId, newDeptId, newTechId, reason);
        System.out.println("✓ Ticket transferred and assigned\n");
        return ResponseEntity.ok(ticket);
    }

    // ========== ANALYTICS ==========

    @GetMapping("/technician/{techId}/workload")
    public ResponseEntity<Integer> getTechnicianWorkload(@PathVariable Long techId) {
        System.out.println("GET /api/tickets/technician/" + techId + "/workload - Fetching workload");
        int workload = ticketService.getTechnicianWorkload(techId);
        System.out.println("✓ Workload: " + workload + " active ticket(s)\n");
        return ResponseEntity.ok(workload);
    }

    @GetMapping("/department/{deptId}/workload")
    public ResponseEntity<Integer> getDepartmentWorkload(@PathVariable Long deptId) {
        System.out.println("GET /api/tickets/department/" + deptId + "/workload - Fetching workload");
        int workload = ticketService.getDepartmentWorkload(deptId);
        System.out.println("✓ Workload: " + workload + " active ticket(s)\n");
        return ResponseEntity.ok(workload);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable TicketStatus status) {
        System.out.println("GET /api/tickets/status/" + status + " - Fetching tickets by status");
        List<Ticket> tickets = ticketService.getTicketsByStatus(status);
        System.out.println("✓ Found " + tickets.size() + " ticket(s)\n");
        return ResponseEntity.ok(tickets);
    }
}
