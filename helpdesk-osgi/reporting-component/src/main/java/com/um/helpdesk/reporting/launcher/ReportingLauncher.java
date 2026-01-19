package com.um.helpdesk.reporting.launcher;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.reporting.impl.ReportingServiceImpl;
import java.util.Map;

public class ReportingLauncher {
    public static void main(String[] args) {
        System.out.println("🚀 STARTING REPORTING COMPONENT TEST...");

        ReportingServiceImpl service = new ReportingServiceImpl();

        // --- TEST 1: CRUD Operations ---
        System.out.println("\n--- 1. Testing Manage Saved Reports (CRUD) ---");

        // Create
        SavedReportArchive report = service.generateReport("Monthly_Activity", "CSV");
        System.out.println("Created Report ID: " + report.getId());

        // Read
        System.out.println("Current Reports Count: " + service.getAllSavedReports().size());

        // Update
        report.setReportName("UPDATED_Monthly_Activity");
        service.updateReport(report);

        // Delete
        service.deleteReport(report.getId());
        System.out.println("Count after delete: " + service.getAllSavedReports().size());


        // --- TEST 2: Data Export & Graphs ---
        System.out.println("\n--- 2. Testing Custom Data Export ---");
        service.generateReport("Visual_Trend", "Graph"); // Should generate a file with "Graph" content


        // --- TEST 3: Failure Analysis ---
        System.out.println("\n--- 3. Testing Failure Rate Analysis ---");
        Map<String, Object> failureStats = service.getFailureRateAnalysis();
        System.out.println("Failure Stats: " + failureStats);
        // Expected: Should show failure rate > 0% based on our MockTicket T2 and T4


        // --- TEST 4: Trends Forecast ---
        System.out.println("\n--- 4. Testing Ticket Trends ---");
        Map<String, Integer> trends = service.getTicketTrendForecast();
        System.out.println("Trends: " + trends);

        System.out.println("\n✅ ALL TESTS COMPLETED.");
    }
}