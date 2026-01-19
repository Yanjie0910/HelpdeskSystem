package com.um.helpdesk.reporting.impl;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.service.ReportingService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportingServiceImpl implements ReportingService {

    // In-memory Database for Reports (Req 1)
    private final List<SavedReportArchive> savedReports = new ArrayList<>();

    // --- MOCK DATA SETUP ---
    // Since we don't have the Ticket component, we mock it internally to test logic.
    private static class MockTicket {
        String id;
        String status; // Open, Closed
        LocalDate createdDate;
        LocalDate dueDate;
        LocalDate closedDate;

        public MockTicket(String id, String status, LocalDate created, LocalDate due, LocalDate closed) {
            this.id = id;
            this.status = status;
            this.createdDate = created;
            this.dueDate = due;
            this.closedDate = closed;
        }
    }

    private final List<MockTicket> mockTickets = new ArrayList<>();

    public ReportingServiceImpl() {
        initializeMockTickets();
    }

    private void initializeMockTickets() {
        LocalDate now = LocalDate.now();

        // Past Data (for Trends)
        mockTickets.add(new MockTicket("T1", "Closed", now.minusWeeks(3), now.minusWeeks(3).plusDays(2), now.minusWeeks(3).plusDays(1)));
        mockTickets.add(new MockTicket("T2", "Closed", now.minusWeeks(2), now.minusWeeks(2).plusDays(2), now.minusWeeks(2).plusDays(3))); // LATE

        // Current Data
        mockTickets.add(new MockTicket("T3", "Open", now.minusDays(1), now.plusDays(2), null));
        mockTickets.add(new MockTicket("T4", "Open", now.minusDays(5), now.minusDays(1), null)); // OVERDUE (Failure)

        // Future Spike Simulation (by creating 'Open' tickets due soon)
        mockTickets.add(new MockTicket("T5", "Open", now, now.plusDays(1), null));
        mockTickets.add(new MockTicket("T6", "Open", now, now.plusDays(1), null));
    }
    // -----------------------

    // Functionality 1: CRUD (Create) & Functionality 2: Export
    @Override
    public SavedReportArchive generateReport(String reportType, String format) {
        SavedReportArchive report = new SavedReportArchive();
        long id = System.currentTimeMillis();
        report.setId(id);
        report.setReportName(reportType + " Report - " + LocalDate.now());
        // Note: Ensure your Entity has these fields. If not, use generic 'setData' or similar.
        // Assuming your SavedReportArchive in base-library might need these fields if they aren't there.
        // For now we set what we can.

        // GENERATE CONTENT (Functionality 2)
        StringBuilder content = new StringBuilder();
        content.append("REPORT TYPE: ").append(reportType).append("\n");
        content.append("FORMAT: ").append(format).append("\n");
        content.append("------------------------------------------------\n");

        if ("CSV".equalsIgnoreCase(format)) {
            content.append("TicketID,Status,Created,Due\n");
            for(MockTicket t : mockTickets) {
                content.append(t.id).append(",").append(t.status).append(",").append(t.createdDate).append(",").append(t.dueDate).append("\n");
            }
        } else if ("Graph".equalsIgnoreCase(format)) {
            content.append("[GRAPH DATA REPRESENTATION]\n");
            content.append("Week 1: ** (2)\n");
            content.append("Week 2: **** (4)\n");
            content.append("(Visualizing spikes in text format)\n");
        } else {
            content.append("Standard Report Export...\n");
        }

        // Simulate File Creation
        String userHome = System.getProperty("user.home");
        String fileName = "Report_" + id + "." + (format.equalsIgnoreCase("Graph") ? "txt" : format.toLowerCase());
        String fullPath = userHome + File.separator + "Desktop" + File.separator + fileName;

        // Write to file (Functionality 2)
        try (FileWriter writer = new FileWriter(fullPath)) {
            writer.write(content.toString());
            System.out.println("✅ FILE GENERATED: " + fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        report.setFilePath(fullPath); // Assuming field exists
        savedReports.add(report);
        return report;
    }

    // Functionality 1: CRUD (Read)
    @Override
    public List<SavedReportArchive> getAllSavedReports() {
        return new ArrayList<>(savedReports);
    }

    // Functionality 1: CRUD (Update)
    @Override
    public void updateReport(SavedReportArchive report) {
        for (int i = 0; i < savedReports.size(); i++) {
            if (savedReports.get(i).getId().equals(report.getId())) {
                savedReports.set(i, report);
                System.out.println("🔄 Report Updated: " + report.getReportName());
                return;
            }
        }
        System.err.println("❌ Report not found for update.");
    }

    // Functionality 1: CRUD (Delete)
    @Override
    public void deleteReport(Long id) {
        savedReports.removeIf(r -> r.getId().equals(id));
        System.out.println("🗑️ Report Deleted: " + id);
    }

    // Functionality 4: Forecast Ticket Trends
    @Override
    public Map<String, Integer> getTicketTrendForecast() {
        Map<String, Integer> trends = new LinkedHashMap<>(); // Use LinkedHashMap to keep order
        LocalDate now = LocalDate.now();

        // 1. Group Mock Tickets by "Weeks Ago" (0 = this week, 1 = last week, etc.)
        int[] weeklyCounts = new int[4]; // [3 weeks ago, 2 weeks ago, 1 week ago, current]

        for (MockTicket t : mockTickets) {
            // Only count past/current tickets, ignore future mock ones
            if (t.createdDate.isAfter(now)) continue;

            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(t.createdDate, now);
            int weeksAgo = (int) (daysAgo / 7);

            if (weeksAgo < 4) {
                // Store in array (index 3 is current week, 0 is 3 weeks ago)
                weeklyCounts[3 - weeksAgo]++;
            }
        }

        // 2. Populate the Map with Real Counts
        trends.put("Week 1 (3 weeks ago)", weeklyCounts[0]);
        trends.put("Week 2 (2 weeks ago)", weeklyCounts[1]);
        trends.put("Week 3 (Last week)", weeklyCounts[2]);
        trends.put("Week 4 (Current)", weeklyCounts[3]);

        // 3. Apply Statistical Prediction
        List<Integer> history = new ArrayList<>();
        for (int count : weeklyCounts) history.add(count);

        int prediction = predictNextValue(history);

        trends.put("Week 5 (Forecast)", prediction);

        return trends;
    }

    // Helper: Calculates y = mx + b to predict the next value
    private int predictNextValue(List<Integer> history) {
        int n = history.size();
        if (n < 2) return history.get(0); // Not enough data, return last value

        // x = time (0, 1, 2...), y = ticket count
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += history.get(i);
            sumXY += i * history.get(i);
            sumX2 += i * i;
        }

        // Calculate Slope (m) and Intercept (b)
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Predict next value (x = n)
        int predicted = (int) Math.round(slope * n + intercept);
        return Math.max(0, predicted); // Cannot have negative tickets
    }

    // Functionality 3: Analyze System Failure Rates
    @Override
    public Map<String, Object> getFailureRateAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        LocalDate now = LocalDate.now();

        long totalDue = mockTickets.stream().filter(t -> t.dueDate.isBefore(now) || t.closedDate != null).count();
        long failures = mockTickets.stream()
                .filter(t -> (t.closedDate != null && t.closedDate.isAfter(t.dueDate)) ||
                        (t.status.equals("Open") && t.dueDate.isBefore(now)))
                .count();

        double rate = totalDue == 0 ? 0 : ((double) failures / totalDue) * 100;

        analysis.put("Total Evaluated", totalDue);
        analysis.put("Missed Deadlines", failures);
        analysis.put("Failure Rate", rate + "%");
        return analysis;
    }
}