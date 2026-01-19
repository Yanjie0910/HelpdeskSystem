package com.um.helpdesk.reporting.impl;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketPriority;
import com.um.helpdesk.entity.TicketStatus;
import com.um.helpdesk.service.ReportingService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReportingServiceImpl implements ReportingService {

    private final List<SavedReportArchive> savedReports = new ArrayList<>();

    // REAL ENTITY LIST (Mocked Data)
    private final List<Ticket> mockTickets = new ArrayList<>();

    public ReportingServiceImpl() {
        initializeMockTickets();
    }

    private void initializeMockTickets() {
        LocalDateTime now = LocalDateTime.now();

        // --- WEEK 1 DATA (3 Weeks Ago) - 2 Tickets ---
        createMockTicket("WiFi Down", TicketPriority.MEDIUM, TicketStatus.CLOSED, now.minusWeeks(3));
        createMockTicket("PC Restarting", TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(3));

        // --- WEEK 2 DATA (2 Weeks Ago) - 3 Tickets ---
        createMockTicket("Server Crash", TicketPriority.URGENT, TicketStatus.CLOSED, now.minusWeeks(2));
        createMockTicket("Email Login Issue", TicketPriority.HIGH, TicketStatus.CLOSED, now.minusWeeks(2));
        createMockTicket("Printer Jam", TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(2));

        // --- WEEK 3 DATA (Last Week) - 4 Tickets ---
        createMockTicket("Projector Broken", TicketPriority.MEDIUM, TicketStatus.CLOSED, now.minusWeeks(1));
        createMockTicket("Software Install", TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(1));
        createMockTicket("VPN Access", TicketPriority.HIGH, TicketStatus.CLOSED, now.minusWeeks(1));
        // THIS ONE FAILED SLA (Urgent but took 3 days to close)
        Ticket failedTicket = createMockTicket("Critical DB Error", TicketPriority.URGENT, TicketStatus.CLOSED, now.minusWeeks(1));
        failedTicket.setResolvedAt(now.minusWeeks(1).plusDays(3)); // Took 72 hours (Failure)

        // --- WEEK 4 DATA (Current Week) - 2 Tickets so far ---
        createMockTicket("Mouse Broken", TicketPriority.LOW, TicketStatus.OPEN, now.minusDays(1));
        // THIS ONE IS FAILING NOW (Urgent, Open for 2 days)
        createMockTicket("System Outage", TicketPriority.URGENT, TicketStatus.OPEN, now.minusDays(2));
    }

    // Helper to create tickets easily
    private Ticket createMockTicket(String title, TicketPriority priority, TicketStatus status, LocalDateTime submittedAt) {
        Ticket t = new Ticket();
        t.setId(System.nanoTime()); // Fake ID
        t.setTitle(title);
        t.setPriority(priority);
        t.setStatus(status);
        t.setSubmittedAt(submittedAt);

        // If closed, assume it was closed 2 hours after submission (unless overridden)
        if (status == TicketStatus.CLOSED) {
            t.setResolvedAt(submittedAt.plusHours(2));
            t.setClosedAt(submittedAt.plusHours(4));
        }

        mockTickets.add(t);
        return t;
    }

    // --- FUNCTIONALITY 3: FAILURE RATE ANALYSIS ---
    @Override
    public Map<String, Object> getFailureRateAnalysis() {
        Map<String, Object> analysis = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        long totalEvaluated = 0;
        long missedDeadlines = 0;

        for (Ticket t : mockTickets) {
            // SLA RULES:
            // URGENT: Must be resolved in 24 Hours
            // HIGH: Must be resolved in 48 Hours
            // MEDIUM/LOW: 7 Days

            long slaHoursLimit;
            switch (t.getPriority()) {
                case URGENT: slaHoursLimit = 24; break;
                case HIGH: slaHoursLimit = 48; break;
                default: slaHoursLimit = 168; // 7 days
            }

            LocalDateTime endTime = (t.getResolvedAt() != null) ? t.getResolvedAt() : now;
            long hoursTaken = Duration.between(t.getSubmittedAt(), endTime).toHours();

            totalEvaluated++;

            if (hoursTaken > slaHoursLimit) {
                missedDeadlines++;
            }
        }

        double failureRate = (totalEvaluated == 0) ? 0 : ((double) missedDeadlines / totalEvaluated) * 100;

        analysis.put("Total Tickets Analyzed", totalEvaluated);
        analysis.put("SLA Breaches (Overdue)", missedDeadlines);
        analysis.put("Failure Rate", String.format("%.1f%%", failureRate));
        analysis.put("Risk Level", failureRate > 15.0 ? "CRITICAL" : "STABLE");

        return analysis;
    }

    // --- FUNCTIONALITY 4: STATISTICAL TREND FORECAST ---
    @Override
    public Map<String, Integer> getTicketTrendForecast() {
        Map<String, Integer> trends = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. Group by Weeks Ago (0=Current, 1=Last Week, etc.)
        int[] weeklyCounts = new int[4]; // [Week-3, Week-2, Week-1, Current]

        for (Ticket t : mockTickets) {
            long weeksBetween = ChronoUnit.WEEKS.between(t.getSubmittedAt(), now);
            if (weeksBetween >= 0 && weeksBetween < 4) {
                // Map index: 3 is current, 0 is 3 weeks ago
                int index = 3 - (int) weeksBetween;
                weeklyCounts[index]++;
            }
        }

        // 2. Populate History
        trends.put("Week 1 (3 weeks ago)", weeklyCounts[0]);
        trends.put("Week 2 (2 weeks ago)", weeklyCounts[1]);
        trends.put("Week 3 (Last week)", weeklyCounts[2]);
        trends.put("Week 4 (Current)", weeklyCounts[3]);

        // 3. Perform Linear Regression (Least Squares)
        // X = Time (0, 1, 2), Y = Count
        // We use the first 3 completed weeks for prediction
        List<Integer> history = Arrays.asList(weeklyCounts[0], weeklyCounts[1], weeklyCounts[2]);
        int nextWeekPrediction = predictNextValue(history);

        trends.put("Week 5 (Forecast)", nextWeekPrediction);

        return trends;
    }

    // Math Helper: y = mx + c
    private int predictNextValue(List<Integer> history) {
        int n = history.size();
        if (n < 2) return history.get(n - 1);

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += history.get(i);
            sumXY += i * history.get(i);
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Predict for X = n (the next step)
        int predicted = (int) Math.round(slope * n + intercept);
        return Math.max(0, predicted);
    }

    // --- CRUD OPERATIONS (Unchanged but using Ticket Entity) ---
    @Override
    public SavedReportArchive generateReport(String reportType, String format) {
        SavedReportArchive report = new SavedReportArchive();
        long id = System.currentTimeMillis();
        report.setId(id);
        report.setReportName(reportType + " Report");
        report.setReportType(reportType);
        report.setFileFormat(format);

        // Generate Content (Same as before)
        StringBuilder content = new StringBuilder();
        content.append("REPORT: ").append(reportType).append("\n");
        content.append("DATE: ").append(LocalDateTime.now()).append("\n\n");

        if ("CSV".equalsIgnoreCase(format)) {
            content.append("ID,Title,Priority,Status,Submitted\n");
            for(Ticket t : mockTickets) {
                content.append(t.getId()).append(",")
                        .append(t.getTitle()).append(",")
                        .append(t.getPriority()).append(",")
                        .append(t.getStatus()).append(",")
                        .append(t.getSubmittedAt()).append("\n");
            }
        } else {
            long open = mockTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
            content.append("Total Tickets: ").append(mockTickets.size()).append("\n");
            content.append("Open: ").append(open).append("\n");
        }

        // --- FIX: ROBUST FILE PATH HANDLING ---
        String userHome = System.getProperty("user.home");
        File directory = new File(userHome, "Desktop");

        // If "Desktop" doesn't exist (e.g., OneDrive issue), fallback to user home
        if (!directory.exists()) {
            directory = new File(userHome);
        }

        File file = new File(directory, reportType + "_" + id + "." + format.toLowerCase());

        // Ensure parent folders exist to prevent "Path Not Found" error
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        report.setFilePath(file.getAbsolutePath());

        // Write File
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content.toString());
            System.out.println("✅ FILE CREATED: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ FAILED TO WRITE FILE: " + e.getMessage());
            e.printStackTrace();
        }
        // --------------------------------------

        savedReports.add(report);
        return report;
    }

    @Override
    public List<SavedReportArchive> getAllSavedReports() { return new ArrayList<>(savedReports); }
    @Override
    public void deleteReport(Long id) { savedReports.removeIf(r -> r.getId().equals(id)); }
    @Override
    public void updateReport(SavedReportArchive report) {
        for(int i=0; i<savedReports.size(); i++) {
            if(savedReports.get(i).getId().equals(report.getId())) {
                savedReports.set(i, report);
                return;
            }
        }
    }
}