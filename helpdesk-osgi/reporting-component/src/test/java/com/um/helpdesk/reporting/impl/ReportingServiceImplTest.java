package com.um.helpdesk.reporting.impl;

import com.um.helpdesk.entity.SavedReportArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportingServiceImplTest {

    private ReportingServiceImpl reportingService;

    @BeforeEach
    void setUp() {
        // The constructor automatically initializes the Mock Data
        reportingService = new ReportingServiceImpl();
    }

    // --- TEST FUNCTIONALITY 1: MANAGE SAVED REPORTS (CRUD) ---
    @Test
    void testGenerateAndSaveReport() {
        SavedReportArchive report = reportingService.generateReport("UnitTest_OSGi", "CSV");

        assertNotNull(report);
        assertEquals("UnitTest_OSGi", report.getReportType());
        assertEquals(1, reportingService.getAllSavedReports().size());

        // Clean up file
        File f = new File(report.getFilePath());
        if (f.exists()) f.delete();
    }

    @Test
    void testDeleteReport() {
        SavedReportArchive report = reportingService.generateReport("ToDelete", "CSV");
        Long id = report.getId();

        reportingService.deleteReport(id);

        assertTrue(reportingService.getAllSavedReports().isEmpty());

        // Clean up file
        File f = new File(report.getFilePath());
        if (f.exists()) f.delete();
    }

    // --- TEST FUNCTIONALITY 2: DATA EXPORT (File Creation) ---
    @Test
    void testPhysicalFileCreation() {
        SavedReportArchive report = reportingService.generateReport("FileCheck_OSGi", "Text");

        File file = new File(report.getFilePath());
        assertTrue(file.exists(), "Physical file should exist on Desktop/Home");
        assertTrue(file.length() > 0, "File should not be empty");

        // Clean up
        file.delete();
    }

    // --- TEST FUNCTIONALITY 3: FAILURE RATE ANALYSIS ---
    @Test
    void testFailureRateAnalysis() {
        Map<String, Object> analysis = reportingService.getFailureRateAnalysis();

        // Based on the mock data in your ServiceImpl:
        // We have mock tickets, and some are explicitly set to fail SLA.

        assertNotNull(analysis);
        assertTrue((long) analysis.get("Total Tickets Analyzed") > 0);

        // We expect at least 1 breach based on your mock data:
        // "failedTicket.setResolvedAt(now.minusWeeks(1).plusDays(3));" -> 72h taken for URGENT (24h limit)
        long breaches = (long) analysis.get("SLA Breaches (Overdue)");
        assertTrue(breaches >= 1, "Should detect at least 1 SLA breach");

        String risk = (String) analysis.get("Risk Level");
        assertNotNull(risk);
    }

    // --- TEST FUNCTIONALITY 4: TREND FORECAST ---
    @Test
    void testTrendForecast() {
        Map<String, Integer> trends = reportingService.getTicketTrendForecast();

        assertNotNull(trends);
        assertTrue(trends.containsKey("Week 1 (3 weeks ago)"));
        assertTrue(trends.containsKey("Week 4 (Current)"));
        assertTrue(trends.containsKey("Week 5 (Forecast)"));

        int forecast = trends.get("Week 5 (Forecast)");
        assertTrue(forecast >= 0, "Forecast cannot be negative");
    }
}