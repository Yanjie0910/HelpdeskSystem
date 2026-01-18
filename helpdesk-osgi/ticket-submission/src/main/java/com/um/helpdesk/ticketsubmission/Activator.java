package com.um.helpdesk.ticketsubmission;

import com.um.helpdesk.service.TicketSubmissionService;
import com.um.helpdesk.ticketsubmission.impl.TicketSubmissionServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Activator implements BundleActivator {

    private ServiceRegistration<TicketSubmissionService> reg;
    private volatile boolean running = true;

    @Override
    public void start(BundleContext context) {
        System.out.println(">>> Ticket Submission Component: Starting...");

        try {
            TicketSubmissionServiceImpl impl = new TicketSubmissionServiceImpl();
            System.out.println(">>> Storage: In-Memory mode (no JPA).");

            if (context != null) {
                Dictionary<String, String> props = new Hashtable<>();
                props.put("component", "ticket-submission");
                reg = context.registerService(TicketSubmissionService.class, impl, props);

                System.out.println(">>> OSGi: TicketSubmissionService registered.");
                System.out.println(">>> (Karaf mode) Demo menu is DISABLED to avoid System.in conflict.");
                return;
            }

            System.out.println(">>> (Simulation mode) Starting console demo...");
            runConsoleDemo(impl);

        } catch (Exception e) {
            System.err.println("!!! ERROR in Ticket Submission Activator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop(BundleContext context) {
        running = false;
        System.out.println("Stopping Ticket Submission Component...");
        try {
            if (reg != null) {
                reg.unregister();
                reg = null;
            }
        } catch (Exception ignored) {}
    }

    private void runConsoleDemo(TicketSubmissionServiceImpl service) {
        Scanner sc = new Scanner(System.in);
        while (running) {
            menu();
            System.out.print("Choose option: ");

            String input = sc.nextLine().trim();
            if (input.isBlank()) continue;

            int c;
            try {
                c = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
                continue;
            }

            if (c == 0) break;

            try {
                handle(c, service, sc);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println(">>> Ticket Submission Demo exited.");
    }

    private void menu() {
        System.out.println("\n--- TICKET SUBMISSION MENU (Use Case Mapping) ---");
        System.out.println("1. [UC14] Lodge New Helpdesk Ticket  (includes UC17, extends UC18)");
        System.out.println("2. [UC15] View & Track My Ticket Status (extends UC19)");
        System.out.println("3. [UC16] View Ticket Details & Communication Timeline (extends UC20; shows UC18 attachments)");
        System.out.println("4. [UC17] Submit Feedback for Completed Tickets");
        System.out.println("9. (Demo Helper) Update Ticket Status NEW/IN_PROGRESS/COMPLETED (to create scenarios)");
        System.out.println("0. Exit Demo");
    }

    private void handle(int c, TicketSubmissionServiceImpl service, Scanner sc) {
        switch (c) {
            case 1 -> DemoFlows.uc14_lodgeNewTicket(service, sc);
            case 2 -> DemoFlows.uc15_viewAndTrack(service, sc);
            case 3 -> DemoFlows.uc16_viewDetailsAndTimeline(service, sc);
            case 4 -> DemoFlows.uc17_submitFeedback(service, sc);
            case 9 -> DemoFlows.demo_updateStatus(service, sc);
            default -> System.out.println("Unknown option.");
        }
    }
}
