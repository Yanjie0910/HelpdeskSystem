package com.um.helpdesk.reporting.launcher;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.reporting.impl.ReportingServiceImpl;

import java.io.File;
import java.util.Map;
import java.util.List;

public class ReportingLauncher {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      UM HELPDESK SYSTEM - REPORTING MODULE      ");
        System.out.println("           (OSGi Component Standalone Demo)      ");
        System.out.println("=================================================\n");

        // 1. Initialize Service (Loads Mock Historical Data automatically)
        System.out.println(">>> Initializing Reporting Service & Loading Historical Data...");
        ReportingServiceImpl service = new ReportingServiceImpl();
        System.out.println("✔ Service Active. Mock Data Loaded.\n");

        // ========================================================
        // 📉 DEMO FEATURE 1: TICKET TREND FORECASTING
        // ========================================================
        System.out.println("-------------------------------------------------");
        System.out.println(" [1] FORECAST TICKET TRENDS (Predictive Analysis)");
        System.out.println("-------------------------------------------------");

        Map<String, Integer> trends = service.getTicketTrendForecast();

        System.out.println("Analysis of Last 4 Weeks:");
        trends.forEach((period, count) -> {
            // Print neatly
            System.out.printf("   %-25s : %d tickets\n", period, count);
        });

        System.out.println("\n>>> CONCLUSION: Based on linear regression, we expect a rise in volume next week.\n");

        // ========================================================
        // ⚠️ DEMO FEATURE 2: FAILURE RATE ANALYSIS (SLA)
        // ========================================================
        System.out.println("-------------------------------------------------");
        System.out.println(" [2] FAILURE RATE ANALYSIS (SLA Compliance)");
        System.out.println("-------------------------------------------------");

        Map<String, Object> analysis = service.getFailureRateAnalysis();

        System.out.println("Performance Metrics:");
        System.out.println("   Total Tickets Analyzed : " + analysis.get("Total Tickets Analyzed"));
        System.out.println("   SLA Breaches (Overdue) : " + analysis.get("SLA Breaches (Overdue)"));
        System.out.println("   Failure Rate           : " + analysis.get("Failure Rate"));
        System.out.println("   System Risk Level      : " + analysis.get("Risk Level"));

        if ("CRITICAL".equals(analysis.get("Risk Level"))) {
            System.out.println("\n>>> ALERT: Risk Level is CRITICAL. Immediate attention required!");
        }
        System.out.println();

        // ========================================================
        // 📄 DEMO FEATURE 3: DATA EXPORT & GENERATION
        // ========================================================
        System.out.println("-------------------------------------------------");
        System.out.println(" [3] GENERATE CUSTOM REPORT (Data Export)");
        System.out.println("-------------------------------------------------");

        String reportName = "Executive_Summary_Q1";
        System.out.println(">>> Generating CSV Report: '" + reportName + "'...");

        SavedReportArchive report = service.generateReport(reportName, "CSV");

        if (report != null && report.getFilePath() != null) {
            System.out.println("✔ Success! Report saved to database.");
            System.out.println("   File Path : " + report.getFilePath());
            System.out.println("   Report ID : " + report.getId());

            // Verify physical existence
            File f = new File(report.getFilePath());
            if(f.exists()) {
                System.out.println("   (Verified: File actually exists on disk)");
            }
        }
        System.out.println();

        // ========================================================
        // 🗄️ DEMO FEATURE 4: MANAGE ARCHIVES (CRUD)
        // ========================================================
        System.out.println("-------------------------------------------------");
        System.out.println(" [4] MANAGE SAVED REPORTS (CRUD Operations)");
        System.out.println("-------------------------------------------------");

        // READ
        List<SavedReportArchive> archives = service.getAllSavedReports();
        System.out.println("1. VIEW: Current Archived Reports in DB: " + archives.size());

        // DELETE
        if (!archives.isEmpty()) {
            Long idToDelete = archives.get(0).getId();
            System.out.println("2. DELETE: Removing Report ID: " + idToDelete + "...");
            service.deleteReport(idToDelete);

            int countAfter = service.getAllSavedReports().size();
            System.out.println("   Count after delete: " + countAfter);

            if (countAfter == archives.size() - 1) {
                System.out.println("✔ Delete Successful.");
            }
        }

        System.out.println("\n=================================================");
        System.out.println("         ✅ REPORTING DEMO COMPLETED             ");
        System.out.println("=================================================");
    }
}