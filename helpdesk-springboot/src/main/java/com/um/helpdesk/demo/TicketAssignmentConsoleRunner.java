package com.um.helpdesk.demo;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.TicketService;
import com.um.helpdesk.repository.DepartmentRepository;
import com.um.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class TicketAssignmentConsoleRunner {

    private final TicketService ticketService;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public TicketAssignmentConsoleRunner(TicketService ticketService,
                                         DepartmentRepository departmentRepository,
                                         UserRepository userRepository) {
        this.ticketService = ticketService;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // ========== FUNCTIONALITY 1: Automated Departmental Routing ==========

    public void viewAllTickets(Scanner sc, User currentUser) {
        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("              ALL TICKETS");
        System.out.println("════════════════════════════════════════════════════");

        List<Ticket> tickets = ticketService.getAllTickets();

        if (tickets.isEmpty()) {
            System.out.println("No tickets found.");
            System.out.println();
            return;
        }

        for (Ticket ticket : tickets) {
            displayTicketSummary(ticket);
        }

        System.out.println("════════════════════════════════════════════════════\n");
    }

    public void autoRouteTicket(Scanner sc, User currentUser) {
        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("       FUNCTIONALITY 1: AUTO-ROUTE TICKET");
        System.out.println("════════════════════════════════════════════════════");

        viewUnassignedTickets(sc, currentUser);

        System.out.print("Enter Ticket ID to auto-route (0 to cancel): ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        if (ticketId == 0) {
            System.out.println("Cancelled.\n");
            return;
        }

        try {
            Ticket ticket = ticketService.autoRouteAndAssign(ticketId);
            System.out.println("\n✓ Success!");
            System.out.println("  Routed to: " + ticket.getAssignedDepartment().getName());
            if (ticket.getAssignedTo() != null) {
                System.out.println("  Assigned to: " + ticket.getAssignedTo().getFullName());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 2: Ticket Claiming and Self-Assignment ==========

    public void viewAssignedTickets(Scanner sc, User currentUser) {
        if (!(currentUser instanceof TechnicianSupportStaff)) {
            System.out.println("\nOnly technicians can view assigned tickets.\n");
            return;
        }

        TechnicianSupportStaff technician = (TechnicianSupportStaff) currentUser;

        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("              MY ASSIGNED TICKETS");
        System.out.println("════════════════════════════════════════════════════");

        List<Ticket> tickets = ticketService.getTicketsByTechnician(technician.getId());

        if (tickets.isEmpty()) {
            System.out.println("You have no assigned tickets.");
            System.out.println();
            return;
        }

        for (Ticket ticket : tickets) {
            displayTicketSummary(ticket);
        }

        System.out.println("════════════════════════════════════════════════════\n");
    }

    public void claimTicket(Scanner sc, User currentUser) {
        if (!(currentUser instanceof TechnicianSupportStaff)) {
            System.out.println("\nOnly technicians can claim tickets.\n");
            return;
        }

        TechnicianSupportStaff technician = (TechnicianSupportStaff) currentUser;

        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("    FUNCTIONALITY 2: TICKET CLAIMING (SELF-ASSIGN)");
        System.out.println("════════════════════════════════════════════════════");

        // Show claimable tickets
        List<Ticket> claimableTickets = ticketService.getClaimableTickets(technician.getId());

        if (claimableTickets.isEmpty()) {
            System.out.println("No tickets available to claim in your department.");
            System.out.println();
            return;
        }

        System.out.println("Available tickets in " + technician.getDepartment().getName() + ":\n");
        for (Ticket ticket : claimableTickets) {
            displayTicketSummary(ticket);
        }

        System.out.print("Enter Ticket ID to claim (0 to cancel): ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        if (ticketId == 0) {
            System.out.println("Cancelled.\n");
            return;
        }

        try {
            Ticket ticket = ticketService.claimTicket(ticketId, technician.getId());
            System.out.println("\n✓ Success! You claimed ticket #" + ticketId);
            System.out.println("  Status changed: OPEN → ASSIGNED");
            System.out.println();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 3: Internal Re-assignment and Chaining ==========

    public void reassignTicket(Scanner sc, User currentUser) {
        if (!(currentUser instanceof TechnicianSupportStaff)) {
            System.out.println("\nOnly technicians can reassign tickets.\n");
            return;
        }

        TechnicianSupportStaff technician = (TechnicianSupportStaff) currentUser;

        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("   FUNCTIONALITY 3: INTERNAL RE-ASSIGNMENT");
        System.out.println("════════════════════════════════════════════════════");

        // Show my tickets
        List<Ticket> myTickets = ticketService.getTicketsByTechnician(technician.getId());

        if (myTickets.isEmpty()) {
            System.out.println("You have no tickets to reassign.");
            System.out.println();
            return;
        }

        System.out.println("Your assigned tickets:\n");
        for (Ticket ticket : myTickets) {
            displayTicketSummary(ticket);
        }

        System.out.print("Enter Ticket ID to reassign (0 to cancel): ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        if (ticketId == 0) {
            System.out.println("Cancelled.\n");
            return;
        }

        // Show technicians in same department
        System.out.println("\nTechnicians in " + technician.getDepartment().getName() + ":");
        List<User> allUsers = userRepository.findAll();
        int count = 1;
        for (User user : allUsers) {
            if (user instanceof TechnicianSupportStaff) {
                TechnicianSupportStaff tech = (TechnicianSupportStaff) user;
                if (tech.getDepartment() != null &&
                    tech.getDepartment().getId().equals(technician.getDepartment().getId()) &&
                    !tech.getId().equals(technician.getId())) {
                    System.out.println(count + ". [ID:" + tech.getId() + "] " + tech.getFullName() +
                                     " (" + tech.getSpecialization() + ")");
                    count++;
                }
            }
        }

        System.out.print("\nEnter Technician ID to reassign to: ");
        Long newTechId = sc.nextLong();
        sc.nextLine();

        System.out.print("Reason for reassignment: ");
        String reason = sc.nextLine();

        try {
            Ticket ticket = ticketService.reassignTicketInternally(ticketId, newTechId, reason);
            System.out.println("\n✓ Success! Ticket reassigned to " + ticket.getAssignedTo().getFullName());
            System.out.println("  Reassignment count: " + ticket.getReassignmentCount());
            System.out.println();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage() + "\n");
        }
    }

    public void viewAssignmentHistory(Scanner sc, User currentUser) {
        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("         ASSIGNMENT HISTORY (CHAIN)");
        System.out.println("════════════════════════════════════════════════════");

        System.out.print("Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        try {
            List<TechnicianSupportStaff> history = ticketService.getAssignmentHistory(ticketId);

            if (history.isEmpty()) {
                System.out.println("No assignment history found.");
            } else {
                System.out.println("\nAssignment Chain:");
                for (int i = 0; i < history.size(); i++) {
                    System.out.println((i + 1) + ". " + history.get(i).getFullName() +
                                     " (" + history.get(i).getDepartment().getName() + ")");
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== FUNCTIONALITY 4: Inter-Departmental Transfer ==========

    public void transferTicket(Scanner sc, User currentUser) {
        if (!(currentUser instanceof TechnicianSupportStaff)) {
            System.out.println("\nOnly technicians can transfer tickets.\n");
            return;
        }

        TechnicianSupportStaff technician = (TechnicianSupportStaff) currentUser;

        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("   FUNCTIONALITY 4: INTER-DEPARTMENTAL TRANSFER");
        System.out.println("════════════════════════════════════════════════════");

        // Show my tickets
        List<Ticket> myTickets = ticketService.getTicketsByTechnician(technician.getId());

        if (myTickets.isEmpty()) {
            System.out.println("You have no tickets to transfer.");
            System.out.println();
            return;
        }

        System.out.println("Your assigned tickets:\n");
        for (Ticket ticket : myTickets) {
            displayTicketSummary(ticket);
        }

        System.out.print("Enter Ticket ID to transfer (0 to cancel): ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        if (ticketId == 0) {
            System.out.println("Cancelled.\n");
            return;
        }

        // Show all departments
        System.out.println("\nAvailable Departments:");
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            if (!dept.getId().equals(technician.getDepartment().getId())) {
                System.out.println("[ID:" + dept.getId() + "] " + dept.getName() + " (" + dept.getCode() + ")");
            }
        }

        System.out.print("\nEnter Department ID to transfer to: ");
        Long newDeptId = sc.nextLong();
        sc.nextLine();

        System.out.print("Reason for transfer: ");
        String reason = sc.nextLine();

        try {
            Ticket ticket = ticketService.transferTicketToDepartment(ticketId, newDeptId, reason);
            System.out.println("\n✓ Success! Ticket transferred to " + ticket.getAssignedDepartment().getName());
            System.out.println("  Status reset to: OPEN (available for new department to claim)");
            System.out.println();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage() + "\n");
        }
    }

    // ========== HELPER METHODS ==========

    private void viewUnassignedTickets(Scanner sc, User currentUser) {
        System.out.println("\nUnassigned Tickets (OPEN status):\n");

        List<Ticket> tickets = ticketService.getTicketsByStatus(TicketStatus.OPEN);

        if (tickets.isEmpty()) {
            System.out.println("No unassigned tickets.");
            return;
        }

        for (Ticket ticket : tickets) {
            displayTicketSummary(ticket);
        }
    }

    private void displayTicketSummary(Ticket ticket) {
        System.out.println("─────────────────────────────────────────────────");
        System.out.println("Ticket #" + ticket.getId() + " | " + ticket.getTitle());
        System.out.println("Status: " + ticket.getStatus() + " | Priority: " + ticket.getPriority());
        System.out.println("Category: " + (ticket.getCategory() != null ? ticket.getCategory() : "N/A"));
        System.out.println("Submitted by: " + ticket.getSubmittedBy().getFullName());

        if (ticket.getAssignedDepartment() != null) {
            System.out.println("Department: " + ticket.getAssignedDepartment().getName());
        } else {
            System.out.println("Department: Not assigned yet");
        }

        if (ticket.getAssignedTo() != null) {
            System.out.println("Assigned to: " + ticket.getAssignedTo().getFullName());
        } else {
            System.out.println("Assigned to: Unassigned (available to claim)");
        }

        System.out.println("Submitted: " + ticket.getSubmittedAt());
    }
}
