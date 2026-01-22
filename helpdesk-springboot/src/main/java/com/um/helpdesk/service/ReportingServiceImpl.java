package com.um.helpdesk.service;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketPriority;
import com.um.helpdesk.entity.TicketStatus;
import com.um.helpdesk.repository.SavedReportRepository;
import com.um.helpdesk.repository.TicketRepository; // IMPORTED
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    @Autowired
    private SavedReportRepository reportRepository;

    @Autowired
    private TicketRepository ticketRepository; // INJECTED REAL REPOSITORY

    // REMOVED MOCK DATA INITIALIZATION

    @Override
    public SavedReportArchive generateReport(String reportType, String format) {
        SavedReportArchive report = new SavedReportArchive();
        report.setReportName(reportType + "_" + System.currentTimeMillis());
        report.setReportType(reportType);
        report.setFileFormat(format);

        // 1. Fetch Real Data
        List<Ticket> tickets = ticketRepository.findAll();

        // Optional: Filter based on reportType if needed
        if ("OpenTickets".equalsIgnoreCase(reportType)) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.OPEN)
                    .collect(Collectors.toList());
        } else if ("Performance".equalsIgnoreCase(reportType)) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.CLOSED || t.getStatus() == TicketStatus.RESOLVED)
                    .collect(Collectors.toList());
        }

        // 2. Generate Content
        StringBuilder content = new StringBuilder();
        content.append("HELPDESK SYSTEM REPORT\n");
        content.append("Type: ").append(reportType).append("\n");
        content.append("Generated: ").append(LocalDateTime.now()).append("\n");
        content.append("Total Records: ").append(tickets.size()).append("\n\n");

        if ("CSV".equalsIgnoreCase(format)) {
            // Functionality 2: Custom Data Export (CSV)
            content.append("ID,Title,Priority,Status,Submitted Date,Resolved Date\n");
            for (Ticket t : tickets) {
                content.append(t.getId()).append(",")
                        .append(escapeCsv(t.getTitle())).append(",")
                        .append(t.getPriority()).append(",")
                        .append(t.getStatus()).append(",")
                        .append(t.getSubmittedAt()).append(",")
                        .append(t.getResolvedAt() != null ? t.getResolvedAt() : "N/A")
                        .append("\n");
            }
        } else {
            // Text Summary / Simple View
            content.append("--- TICKET SUMMARY ---\n");
            Map<TicketStatus, Long> statusCount = tickets.stream()
                    .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

            statusCount.forEach((k, v) -> content.append(k).append(": ").append(v).append("\n"));

            content.append("\n--- DETAILS ---\n");
            for(Ticket t : tickets) {
                content.append(String.format("[%s] %s - %s (%s)\n",
                        t.getId(), t.getTitle(), t.getStatus(), t.getPriority()));
            }
        }

        // 3. Write to File
        String userHome = System.getProperty("user.home");
        File directory = new File(userHome, "Desktop/HelpdeskReports");
        if (!directory.exists()) directory.mkdirs();

        String fileName = reportType + "_" + System.currentTimeMillis() + "." + format.toLowerCase();
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content.toString());
            System.out.println("✅ REAL DATA REPORT CREATED: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace(); // Handle appropriately in production
        }

        report.setFilePath(file.getAbsolutePath());

        // Functionality 1: Manage Saved Reports (Save to DB)
        return reportRepository.save(report);
    }

    @Override
    public Map<String, Object> getFailureRateAnalysis() {
        // Functionality 3: Analyze System Failure Rates (SLA Breaches)
        Map<String, Object> analysis = new LinkedHashMap<>();
        List<Ticket> allTickets = ticketRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        long totalEvaluated = 0;
        long missedDeadlines = 0;

        for (Ticket t : allTickets) {
            // Define SLA Rules (Hours)
            long slaHoursLimit = getSlaLimitHours(t.getPriority());

            // Calculate Time Taken (if resolved) or Time Elapsed (if open)
            LocalDateTime endTime = (t.getResolvedAt() != null) ? t.getResolvedAt() : now;

            // Skip tickets submitted in the future (sanity check)
            if (t.getSubmittedAt().isAfter(now)) continue;

            long hoursElapsed = Duration.between(t.getSubmittedAt(), endTime).toHours();
            totalEvaluated++;

            if (hoursElapsed > slaHoursLimit) {
                missedDeadlines++;
            }
        }

        double failureRate = (totalEvaluated == 0) ? 0 : ((double) missedDeadlines / totalEvaluated) * 100;

        analysis.put("Total Tickets Analyzed", totalEvaluated);
        analysis.put("Overdue Tickets", missedDeadlines);
        analysis.put("Failure Rate (%)", String.format("%.2f", failureRate));
        analysis.put("System Health", failureRate < 10.0 ? "GOOD" : (failureRate < 25.0 ? "WARNING" : "CRITICAL"));

        return analysis;
    }

    @Override
    public Map<String, Integer> getTicketTrendForecast() {
        // Functionality 4: Forecast Ticket Trends (Linear Regression on Real Data)
        Map<String, Integer> trends = new LinkedHashMap<>();
        List<Ticket> tickets = ticketRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // Bucket tickets by "Weeks Ago" (0 = this week, 1 = last week, etc.)
        // We will look at the last 4 weeks
        int[] weeklyCounts = new int[4];

        for (Ticket t : tickets) {
            long weeksAgo = ChronoUnit.WEEKS.between(t.getSubmittedAt(), now);
            if (weeksAgo >= 0 && weeksAgo < 4) {
                // Map weeksAgo 0 -> index 3 (Now), 3 -> index 0 (Oldest)
                int index = 3 - (int) weeksAgo;
                weeklyCounts[index]++;
            }
        }

        trends.put("3 Weeks Ago", weeklyCounts[0]);
        trends.put("2 Weeks Ago", weeklyCounts[1]);
        trends.put("Last Week", weeklyCounts[2]);
        trends.put("Current Week", weeklyCounts[3]);

        // Predict Next Week using Linear Regression
        List<Integer> history = Arrays.asList(weeklyCounts[0], weeklyCounts[1], weeklyCounts[2], weeklyCounts[3]);
        int predicted = predictNextValue(history);

        trends.put("Next Week (Forecast)", predicted);

        return trends;
    }

    // Helper: SLA Logic
    private long getSlaLimitHours(TicketPriority priority) {
        if (priority == null) return 168; // Default Low
        switch (priority) {
            case URGENT: return 24;
            case HIGH: return 48;
            case MEDIUM: return 72;
            case LOW: default: return 168; // 1 Week
        }
    }

    // Helper: CSV Escape
    private String escapeCsv(String data) {
        if (data == null) return "";
        return "\"" + data.replace("\"", "\"\"") + "\"";
    }

    // Helper: Simple Linear Regression
    private int predictNextValue(List<Integer> history) {
        int n = history.size();
        if (n < 2) return history.get(n - 1); // Not enough data

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += history.get(i);
            sumXY += i * history.get(i);
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Predict for x = n (the next point)
        return Math.max(0, (int) Math.round(slope * n + intercept));
    }

    @Override
    public List<SavedReportArchive> getAllSavedReports() {
        return reportRepository.findAll();
    }

    @Override
    public void deleteReport(Long id) {
        // Optional: Also delete the physical file
        reportRepository.findById(id).ifPresent(report -> {
            if (report.getFilePath() != null) {
                File f = new File(report.getFilePath());
                if (f.exists()) f.delete();
            }
        });
        reportRepository.deleteById(id);
    }

    @Override
    public void updateReport(SavedReportArchive report) {
        reportRepository.save(report);
    }
}