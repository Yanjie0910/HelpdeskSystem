package com.um.helpdesk.ticketassignment;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.TicketService;
import com.um.helpdesk.ticketassignment.impl.TicketServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

public class Activator implements BundleActivator {

    private EntityManagerFactory emf;
    private EntityManager em;
    private ServiceRegistration<TicketService> serviceRegistration;
    private boolean running = true;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println(">>> Ticket Assignment Component: Starting...");

        new Thread(() -> {
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                System.out.println(">>> JPA: Initializing EntityManagerFactory...");
                emf = Persistence.createEntityManagerFactory("helpdesk-pu");
                em = emf.createEntityManager();
                System.out.println(">>> JPA: Initialized successfully.");

                TicketServiceImpl serviceImpl = new TicketServiceImpl(em);

                // Register OSGi Service (only if running in real OSGi container)
                if (context != null) {
                    Dictionary<String, String> props = new Hashtable<>();
                    props.put("component", "ticket-assignment");
                    serviceRegistration = context.registerService(TicketService.class, serviceImpl, props);
                    System.out.println(">>> OSGi: TicketService registered.");
                } else {
                    System.out.println(">>> Running in standalone mode (no OSGi container).");
                }

                initializeTestData(serviceImpl, em);

                // Run console demo (works in Apache Felix, conflicts with Karaf console)
                runConsoleDemo(serviceImpl, em);

                System.out.println(">>> Ticket Assignment Component: Stopped.");

            } catch (Exception e) {
                System.err.println("!!! ERROR in Ticket Assignment Activator: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        System.out.println(">>> Ticket Assignment Activator thread launched.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        running = false;
        System.out.println("Stopping Ticket Assignment Component...");
        if (serviceRegistration != null)
            serviceRegistration.unregister();
        if (em != null && em.isOpen())
            em.close();
        if (emf != null && emf.isOpen())
            emf.close();
    }

    private void initializeTestData(TicketServiceImpl ticketService, EntityManager em) {
        System.out.println("\n>>> Initializing Test Data...");

        try {
            em.getTransaction().begin();

            // Create Departments
            Department itDept = new Department("Information Technology", "IT");
            Department facilitiesDept = new Department("Facilities Management", "FACILITIES");
            Department financeDept = new Department("Finance", "FINANCE");
            Department academicDept = new Department("Academic Affairs", "ACADEMIC");

            em.persist(itDept);
            em.persist(facilitiesDept);
            em.persist(financeDept);
            em.persist(academicDept);

            // Create Technicians
            TechnicianSupportStaff techIT1 = new TechnicianSupportStaff();
            techIT1.setEmail("tech.it1@um.edu.my");
            techIT1.setFullName("Ahmad IT Specialist");
            techIT1.setStaffId("IT001");
            techIT1.setDepartment(itDept);
            techIT1.setSpecialization("Network & Security");
            techIT1.setRole(UserRole.TECHNICIAN);
            techIT1.setActive(true);

            TechnicianSupportStaff techIT2 = new TechnicianSupportStaff();
            techIT2.setEmail("tech.it2@um.edu.my");
            techIT2.setFullName("Siti Software Expert");
            techIT2.setStaffId("IT002");
            techIT2.setDepartment(itDept);
            techIT2.setSpecialization("Software Support");
            techIT2.setRole(UserRole.TECHNICIAN);
            techIT2.setActive(true);

            TechnicianSupportStaff techFacilities = new TechnicianSupportStaff();
            techFacilities.setEmail("tech.facilities@um.edu.my");
            techFacilities.setFullName("Kumar Facilities Manager");
            techFacilities.setStaffId("FAC001");
            techFacilities.setDepartment(facilitiesDept);
            techFacilities.setSpecialization("Building Maintenance");
            techFacilities.setRole(UserRole.TECHNICIAN);
            techFacilities.setActive(true);

            em.persist(techIT1);
            em.persist(techIT2);
            em.persist(techFacilities);

            // Create Sample Student (ticket submitter)
            Student student = new Student();
            student.setEmail("student@um.edu.my");
            student.setFullName("Ali Student");
            student.setRole(UserRole.STUDENT);
            student.setActive(true);
            student.setStudentId("UM123456");

            em.persist(student);

            // Create Sample Tickets
            Ticket ticket1 = new Ticket();
            ticket1.setTitle("Cannot connect to campus WiFi");
            ticket1.setDescription("My laptop cannot connect to the university WiFi network. It shows authentication error.");
            ticket1.setCategory("Network");
            ticket1.setPriority(TicketPriority.HIGH);
            ticket1.setSubmittedBy(student);
            ticket1.setStatus(TicketStatus.OPEN);

            Ticket ticket2 = new Ticket();
            ticket2.setTitle("Air conditioning not working in Room A301");
            ticket2.setDescription("The air conditioning unit in lecture room A301 has not been working for 2 days.");
            ticket2.setCategory("Facilities");
            ticket2.setPriority(TicketPriority.MEDIUM);
            ticket2.setSubmittedBy(student);
            ticket2.setStatus(TicketStatus.OPEN);

            Ticket ticket3 = new Ticket();
            ticket3.setTitle("Software installation request");
            ticket3.setDescription("Need to install MATLAB on computer lab machines for engineering course.");
            ticket3.setCategory("Software");
            ticket3.setPriority(TicketPriority.LOW);
            ticket3.setSubmittedBy(student);
            ticket3.setStatus(TicketStatus.OPEN);

            em.persist(ticket1);
            em.persist(ticket2);
            em.persist(ticket3);

            em.getTransaction().commit();

            System.out.println(">>> Test Data Created:");
            System.out.println("  - 4 Departments (IT, Facilities, Finance, Academic)");
            System.out.println("  - 3 Technicians");
            System.out.println("  - 3 Sample Tickets");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runConsoleDemo(TicketServiceImpl service, EntityManager em) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║   TICKET ASSIGNMENT MODULE - INTERACTIVE DEMO       ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        while (running) {
            displayMenu();
            System.out.print("Choose option: ");
            try {
                if (sc.hasNextInt()) {
                    int choice = sc.nextInt();
                    sc.nextLine();
                    if (choice == 0) {
                        System.out.println("Exiting demo...");
                        break;
                    }
                    handleMenu(choice, service, em, sc);
                } else {
                    sc.nextLine();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║            TICKET ASSIGNMENT MENU                    ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  FUNCTIONALITY 1: Automated Departmental Routing    ║");
        System.out.println("║    1. View All Tickets                               ║");
        System.out.println("║    2. Auto-Route Ticket to Department                ║");
        System.out.println("║    3. Auto-Route and Assign Ticket                   ║");
        System.out.println("║                                                      ║");
        System.out.println("║  FUNCTIONALITY 2: Ticket Claiming & Self-Assignment ║");
        System.out.println("║    4. View Unassigned Tickets for Department        ║");
        System.out.println("║    5. Technician Claims Ticket                       ║");
        System.out.println("║                                                      ║");
        System.out.println("║  FUNCTIONALITY 3: Internal Re-assignment & Chaining ║");
        System.out.println("║    6. Reassign Ticket (Same Department)              ║");
        System.out.println("║    7. View Assignment History                        ║");
        System.out.println("║                                                      ║");
        System.out.println("║  FUNCTIONALITY 4: Inter-Departmental Transfer       ║");
        System.out.println("║    8. Transfer Ticket to Different Department       ║");
        System.out.println("║    9. Transfer and Assign Ticket                     ║");
        System.out.println("║                                                      ║");
        System.out.println("║  ANALYTICS                                           ║");
        System.out.println("║   10. View Technician Workload                       ║");
        System.out.println("║   11. View Department Workload                       ║");
        System.out.println("║                                                      ║");
        System.out.println("║  HELPER VIEWS                                        ║");
        System.out.println("║   12. View All Departments                           ║");
        System.out.println("║   13. View All Technicians                           ║");
        System.out.println("║                                                      ║");
        System.out.println("║    0. Exit Demo                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    private void handleMenu(int choice, TicketServiceImpl service, EntityManager em, Scanner sc) {
        try {
            switch (choice) {
                case 1 -> viewAllTickets(service);
                case 2 -> autoRouteTicket(service, sc);
                case 3 -> autoRouteAndAssignTicket(service, sc);
                case 4 -> viewUnassignedTickets(service, em, sc);
                case 5 -> claimTicket(service, em, sc);
                case 6 -> reassignTicket(service, sc);
                case 7 -> viewAssignmentHistory(service, sc);
                case 8 -> transferTicket(service, sc);
                case 9 -> transferAndAssignTicket(service, sc);
                case 10 -> viewTechnicianWorkload(service, em, sc);
                case 11 -> viewDepartmentWorkload(service, em, sc);
                case 12 -> viewAllDepartments(em);
                case 13 -> viewAllTechnicians(em);
                default -> System.out.println("Invalid option");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void viewAllTickets(TicketServiceImpl service) {
        System.out.println("\n--- ALL TICKETS ---");
        List<Ticket> tickets = service.getAllTickets();
        if (tickets.isEmpty()) {
            System.out.println("No tickets found.");
            return;
        }
        for (Ticket t : tickets) {
            System.out.printf("ID: %d | %s | Status: %s | Priority: %s | Dept: %s | Assigned: %s%n",
                t.getId(), t.getTitle(), t.getStatus(), t.getPriority(),
                t.getAssignedDepartment() != null ? t.getAssignedDepartment().getName() : "None",
                t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned");
        }
    }

    private void autoRouteTicket(TicketServiceImpl service, Scanner sc) {
        System.out.print("Enter Ticket ID to route: ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        Department dept = service.routeTicketToDepartment(ticketId);
        System.out.println("✓ Ticket routed to: " + (dept != null ? dept.getName() : "Unknown"));
    }

    private void autoRouteAndAssignTicket(TicketServiceImpl service, Scanner sc) {
        System.out.print("Enter Ticket ID to route and assign: ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        Ticket ticket = service.autoRouteAndAssign(ticketId);
        System.out.println("✓ Ticket routed and assigned to: " +
            (ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : "No technician available"));
    }

    private void viewUnassignedTickets(TicketServiceImpl service, EntityManager em, Scanner sc) {
        System.out.print("Enter Department ID: ");
        Long deptId = sc.nextLong();
        sc.nextLine();

        List<Ticket> tickets = service.getUnassignedTicketsForDepartment(deptId);
        System.out.println("\n--- UNASSIGNED TICKETS ---");
        if (tickets.isEmpty()) {
            System.out.println("No unassigned tickets for this department.");
            return;
        }
        for (Ticket t : tickets) {
            System.out.printf("ID: %d | %s | Priority: %s%n", t.getId(), t.getTitle(), t.getPriority());
        }
    }

    private void claimTicket(TicketServiceImpl service, EntityManager em, Scanner sc) {
        System.out.print("Enter Ticket ID to claim: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter Technician ID: ");
        Long techId = sc.nextLong();
        sc.nextLine();

        Ticket ticket = service.claimTicket(ticketId, techId);
        System.out.println("✓ Ticket claimed successfully by " + ticket.getAssignedTo().getFullName());
    }

    private void reassignTicket(TicketServiceImpl service, Scanner sc) {
        System.out.print("Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter New Technician ID: ");
        Long newTechId = sc.nextLong();
        sc.nextLine();
        System.out.print("Reason for reassignment: ");
        String reason = sc.nextLine();

        Ticket ticket = service.reassignTicketInternally(ticketId, newTechId, reason);
        System.out.println("✓ Ticket reassigned to " + ticket.getAssignedTo().getFullName());
    }

    private void viewAssignmentHistory(TicketServiceImpl service, Scanner sc) {
        System.out.print("Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        sc.nextLine();

        List<TechnicianSupportStaff> history = service.getAssignmentHistory(ticketId);
        System.out.println("\n--- ASSIGNMENT HISTORY ---");
        if (history.isEmpty()) {
            System.out.println("No assignment history found.");
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i).getFullName());
        }
    }

    private void transferTicket(TicketServiceImpl service, Scanner sc) {
        System.out.print("Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter New Department ID: ");
        Long newDeptId = sc.nextLong();
        sc.nextLine();
        System.out.print("Reason for transfer: ");
        String reason = sc.nextLine();

        Ticket ticket = service.transferTicketToDepartment(ticketId, newDeptId, reason);
        System.out.println("✓ Ticket transferred to " + ticket.getAssignedDepartment().getName());
    }

    private void transferAndAssignTicket(TicketServiceImpl service, Scanner sc) {
        System.out.print("Enter Ticket ID: ");
        Long ticketId = sc.nextLong();
        System.out.print("Enter New Department ID: ");
        Long newDeptId = sc.nextLong();
        System.out.print("Enter New Technician ID: ");
        Long newTechId = sc.nextLong();
        sc.nextLine();
        System.out.print("Reason for transfer: ");
        String reason = sc.nextLine();

        Ticket ticket = service.transferAndAssign(ticketId, newDeptId, newTechId, reason);
        System.out.println("✓ Ticket transferred and assigned to " + ticket.getAssignedTo().getFullName());
    }

    private void viewTechnicianWorkload(TicketServiceImpl service, EntityManager em, Scanner sc) {
        System.out.print("Enter Technician ID: ");
        Long techId = sc.nextLong();
        sc.nextLine();

        TechnicianSupportStaff tech = em.find(TechnicianSupportStaff.class, techId);
        if (tech == null) {
            System.out.println("Technician not found.");
            return;
        }

        int workload = service.getTechnicianWorkload(techId);
        System.out.printf("Technician: %s | Active Tickets: %d%n", tech.getFullName(), workload);
    }

    private void viewDepartmentWorkload(TicketServiceImpl service, EntityManager em, Scanner sc) {
        System.out.print("Enter Department ID: ");
        Long deptId = sc.nextLong();
        sc.nextLine();

        Department dept = em.find(Department.class, deptId);
        if (dept == null) {
            System.out.println("Department not found.");
            return;
        }

        int workload = service.getDepartmentWorkload(deptId);
        System.out.printf("Department: %s | Active Tickets: %d%n", dept.getName(), workload);
    }

    private void viewAllDepartments(EntityManager em) {
        System.out.println("\n--- ALL DEPARTMENTS ---");
        List<Department> departments = em.createQuery("SELECT d FROM Department d", Department.class).getResultList();
        if (departments.isEmpty()) {
            System.out.println("No departments found.");
            return;
        }
        System.out.println("┌─────┬──────────────────┬──────────┐");
        System.out.println("│ ID  │ Department Name  │   Code   │");
        System.out.println("├─────┼──────────────────┼──────────┤");
        for (Department d : departments) {
            System.out.printf("│ %-3d │ %-16s │ %-8s │%n", d.getId(), d.getName(), d.getCode());
        }
        System.out.println("└─────┴──────────────────┴──────────┘");
    }

    private void viewAllTechnicians(EntityManager em) {
        System.out.println("\n--- ALL TECHNICIANS ---");
        List<TechnicianSupportStaff> technicians = em.createQuery(
            "SELECT t FROM TechnicianSupportStaff t", TechnicianSupportStaff.class
        ).getResultList();
        if (technicians.isEmpty()) {
            System.out.println("No technicians found.");
            return;
        }
        System.out.println("┌─────┬─────────────────────┬──────────────────┬───────────────┐");
        System.out.println("│ ID  │ Name                │ Email            │ Department    │");
        System.out.println("├─────┼─────────────────────┼──────────────────┼───────────────┤");
        for (TechnicianSupportStaff t : technicians) {
            System.out.printf("│ %-3d │ %-19s │ %-16s │ %-13s │%n",
                t.getId(),
                t.getFullName(),
                t.getEmail(),
                t.getDepartment() != null ? t.getDepartment().getName() : "None");
        }
        System.out.println("└─────┴─────────────────────┴──────────────────┴───────────────┘");
    }
}
