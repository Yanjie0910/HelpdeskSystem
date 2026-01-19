package com.um.helpdesk.service;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketPriority;
import com.um.helpdesk.entity.TicketStatus;
import com.um.helpdesk.repository.SavedReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReportingServiceImpl implements ReportingService {

    @Autowired
    private SavedReportRepository reportRepository;

    // MOCK DATA (Same as OSGi to ensure consistent stats)
    private final List<Ticket> mockTickets = new ArrayList<>();

    public ReportingServiceImpl() {
        initializeMockTickets();
    }

    private void initializeMockTickets() {
        LocalDateTime now = LocalDateTime.now();
        // Exact same scenarios as your OSGi component
        createMockTicket("WiFi Down", TicketPriority.MEDIUM, TicketStatus.CLOSED, now.minusWeeks(3));
        createMockTicket("PC Restarting", TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(3));
        createMockTicket("Server Crash", TicketPriority.URGENT, TicketStatus.CLOSED, now.minusWeeks(2));
        createMockTicket("Email Login Issue", TicketPriority.HIGH, TicketStatus.CLOSED, now.minusWeeks(2));
        createMockTicket("Printer Jam", TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(2));
        createMockTicket("Projector Broken", TicketPriority.MEDIUM, TicketStatus.CLOSED, now.minusWeeks(1));
        createMockTicket("Software Install", TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(1));
        createMockTicket("VPN Access", TicketPriority.HIGH, TicketStatus.CLOSED, now.minusWeeks(1));

        // FAILURES
        Ticket failedTicket = createMockTicket("Critical DB Error", TicketPriority.URGENT, TicketStatus.CLOSED, now.minusWeeks(1));
        failedTicket.setResolvedAt(now.minusWeeks(1).plusDays(3)); // 72h failure
        createMockTicket("Mouse Broken", TicketPriority.LOW, TicketStatus.OPEN, now.minusDays(1));
        createMockTicket("System Outage", TicketPriority.URGENT, TicketStatus.OPEN, now.minusDays(2)); // Open failure
    }

    private Ticket createMockTicket(String title, TicketPriority priority, TicketStatus status, LocalDateTime submittedAt) {
        Ticket t = new Ticket();
        t.setId(System.nanoTime()); // Spring/Hibernate usually handles IDs, but for mock objects this is fine
        t.setTitle(title);
        t.setPriority(priority);
        t.setStatus(status);
        t.setSubmittedAt(submittedAt);
        if (status == TicketStatus.CLOSED) {
            t.setResolvedAt(submittedAt.plusHours(2));
            t.setClosedAt(submittedAt.plusHours(4));
        }
        mockTickets.add(t);
        return t;
    }

    @Override
    public SavedReportArchive generateReport(String reportType, String format) {
        SavedReportArchive report = new SavedReportArchive();
        // In Spring JPA, ID is usually auto-generated.
        // If your Entity uses @GeneratedValue, don't set ID here.
        // If strictly manual: report.setId(System.currentTimeMillis());

        report.setReportName(reportType + " Report");
        report.setReportType(reportType);
        report.setFileFormat(format);

        // Content Generation
        StringBuilder content = new StringBuilder();
        content.append("SPRING BOOT REPORT: ").append(reportType).append("\n");
        content.append("DATE: ").append(LocalDateTime.now()).append("\n\n");

        if ("CSV".equalsIgnoreCase(format)) {
            content.append("ID,Title,Priority,Status,Submitted\n");
            for (Ticket t : mockTickets) {
                content.append(t.getId()).append(",").append(t.getTitle()).append(",")
                        .append(t.getPriority()).append(",").append(t.getStatus()).append(",")
                        .append(t.getSubmittedAt()).append("\n");
            }
        } else {
            long open = mockTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
            content.append("Total: ").append(mockTickets.size()).append("\nOpen: ").append(open);
        }

        // File Writing (Robust Path)
        String userHome = System.getProperty("user.home");
        File directory = new File(userHome, "Desktop");
        if (!directory.exists()) directory = new File(userHome);

        File file = new File(directory, "Spring_" + reportType + "_" + System.currentTimeMillis() + "." + format.toLowerCase());

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content.toString());
            System.out.println("✅ FILE CREATED: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        report.setFilePath(file.getAbsolutePath());
        return reportRepository.save(report); // Save to Real H2 DB
    }

    @Override
    public Map<String, Object> getFailureRateAnalysis() {
        Map<String, Object> analysis = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        long totalEvaluated = 0;
        long missedDeadlines = 0;

        for (Ticket t : mockTickets) {
            long slaHoursLimit;
            switch (t.getPriority()) {
                case URGENT: slaHoursLimit = 24; break;
                case HIGH: slaHoursLimit = 48; break;
                default: slaHoursLimit = 168; // 7 days
            }

            LocalDateTime endTime = (t.getResolvedAt() != null) ? t.getResolvedAt() : now;
            long hoursTaken = Duration.between(t.getSubmittedAt(), endTime).toHours();
            totalEvaluated++;

            if (hoursTaken > slaHoursLimit) missedDeadlines++;
        }

        double failureRate = (totalEvaluated == 0) ? 0 : ((double) missedDeadlines / totalEvaluated) * 100;
        analysis.put("Total Analyzed", totalEvaluated);
        analysis.put("SLA Breaches", missedDeadlines);
        analysis.put("Failure Rate", String.format("%.1f%%", failureRate));
        analysis.put("Risk Level", failureRate > 15.0 ? "CRITICAL" : "STABLE");
        return analysis;
    }

    @Override
    public Map<String, Integer> getTicketTrendForecast() {
        Map<String, Integer> trends = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        int[] weeklyCounts = new int[4];

        for (Ticket t : mockTickets) {
            long weeksBetween = ChronoUnit.WEEKS.between(t.getSubmittedAt(), now);
            if (weeksBetween >= 0 && weeksBetween < 4) {
                weeklyCounts[3 - (int) weeksBetween]++;
            }
        }

        trends.put("Week 1 (-3)", weeklyCounts[0]);
        trends.put("Week 2 (-2)", weeklyCounts[1]);
        trends.put("Week 3 (-1)", weeklyCounts[2]);
        trends.put("Week 4 (Now)", weeklyCounts[3]);

        // Least Squares Regression
        List<Integer> history = Arrays.asList(weeklyCounts[0], weeklyCounts[1], weeklyCounts[2]);
        trends.put("Week 5 (Forecast)", predictNextValue(history));
        return trends;
    }

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
        return Math.max(0, (int) Math.round(slope * n + intercept));
    }

    @Override
    public List<SavedReportArchive> getAllSavedReports() {
        return reportRepository.findAll();
    }

    @Override
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    public void updateReport(SavedReportArchive report) {
        reportRepository.save(report);
    }
}