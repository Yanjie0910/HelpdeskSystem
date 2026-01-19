package com.um.helpdesk.reporting;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.service.ReportingService;
import com.um.helpdesk.reporting.controller.ReportingController;
import com.um.helpdesk.reporting.impl.ReportingServiceImpl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class Activator implements BundleActivator {

    private EntityManagerFactory emf;
    private EntityManager em;
    private ServiceRegistration<ReportingService> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting Reporting Component...");

        // 1. Init JPA
        emf = Persistence.createEntityManagerFactory("reporting-pu");
        em = emf.createEntityManager();

        // 2. Setup Controller and Service
        ReportingController controller = new ReportingController(em);
        ReportingServiceImpl service = new ReportingServiceImpl();

        // 3. Register Service
        if (context != null) {
            Dictionary<String, String> props = new Hashtable<>();
            props.put("component", "reporting");
            registration = context.registerService(ReportingService.class, service, props);
            System.out.println("ReportingService registered successfully.");
        }

        // 4. Run Demo
        runConsoleDemo(service);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping Reporting Component...");
        if (registration != null) registration.unregister();
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
    }

    private void runConsoleDemo(ReportingService service) {
        Scanner sc = new Scanner(System.in);
        System.out.println("------------------- REPORTING MODULE -------------------");
        boolean running = true;

        while (running) {
            System.out.println("\n1. Generate Report | 2. View Archives | 3. Forecast | 0. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Type (Performance/Traffic): ");
                    String type = sc.nextLine();
                    System.out.print("Format (PDF/CSV): ");
                    String fmt = sc.nextLine();
                    SavedReportArchive r = service.generateReport(type, fmt);
                    System.out.println("Generated: " + r.getReportName() + " (ID: " + r.getId() + ")");
                }
                case 2 -> {
                    System.out.println("--- Archives ---");
                    for (SavedReportArchive r : service.getAllSavedReports()) {
                        System.out.println(r.getId() + ": " + r.getReportName() + " [" + r.getFileFormat() + "]");
                    }
                }
                case 3 -> {
                    System.out.println("--- Forecast ---");
                    System.out.println(service.getTicketTrendForecast());
                }
                case 0 -> running = false;
            }
        }
        System.out.println("✓ Reporting Demo ended.");
    }
}