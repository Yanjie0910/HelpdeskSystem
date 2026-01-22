package com.um.helpdesk.demo;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Scanner;

@Component
public class ReportingConsoleRunner implements CommandLineRunner {

    @Autowired
    private ReportingService reportingService;

    @Override
    public void run(String... args) throws Exception {
        // You can comment this out if you don't want it running every time
        // runReportingDemo();
    }

    public void runReportingDemo() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("📊 REPORTING COMPONENT TEST (SPRING BOOT)");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Generate Report (CSV/PDF)");
            System.out.println("2. View Saved Reports");
            System.out.println("3. Analyze Failure Rates (SLA)");
            System.out.println("4. Forecast Trends");
            System.out.println("5. Delete Report");
            System.out.println("0. Exit Reporting Demo");
            System.out.print("Select an option: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (option) {
                case 1:
                    System.out.print("Enter Report Type (e.g., TicketVolume): ");
                    String type = scanner.nextLine();
                    System.out.print("Enter Format (CSV/Text): ");
                    String format = scanner.nextLine();
                    SavedReportArchive report = reportingService.generateReport(type, format);
                    System.out.println("✅ Report Generated: " + report.getFilePath());
                    break;
                case 2:
                    System.out.println("--- Saved Reports ---");
                    for (SavedReportArchive r : reportingService.getAllSavedReports()) {
                        System.out.println("ID: " + r.getId() + " | " + r.getReportName() + " (" + r.getFileFormat() + ")");
                    }
                    break;
                case 3:
                    Map<String, Object> failures = reportingService.getFailureRateAnalysis();
                    System.out.println("--- Failure Analysis ---");
                    failures.forEach((k, v) -> System.out.println(k + ": " + v));
                    break;
                case 4:
                    Map<String, Integer> trends = reportingService.getTicketTrendForecast();
                    System.out.println("--- Trend Forecast ---");
                    trends.forEach((k, v) -> System.out.println(k + ": " + v));
                    break;
                case 5:
                    System.out.print("Enter Report ID to delete: ");
                    Long id = scanner.nextLong();
                    reportingService.deleteReport(id);
                    System.out.println("🗑️ Report deleted.");
                    break;
                case 0:
                    running = false;
                    System.out.println("Exiting Reporting Demo...");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}