package com.um.helpdesk.controller;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    // 1. Generate Report
    // Usage: POST /api/reports/generate?type=Monthly&format=CSV
    @PostMapping("/generate")
    public ResponseEntity<SavedReportArchive> generateReport(
            @RequestParam String type,
            @RequestParam String format) {
        return ResponseEntity.ok(reportingService.generateReport(type, format));
    }

    // 2. Get All Reports
    @GetMapping
    public ResponseEntity<List<SavedReportArchive>> getAllReports() {
        return ResponseEntity.ok(reportingService.getAllSavedReports());
    }

    // 3. Failure Analysis
    @GetMapping("/analysis/failures")
    public ResponseEntity<Map<String, Object>> getFailureAnalysis() {
        return ResponseEntity.ok(reportingService.getFailureRateAnalysis());
    }

    // 4. Trend Forecast
    @GetMapping("/analysis/trends")
    public ResponseEntity<Map<String, Integer>> getTrends() {
        return ResponseEntity.ok(reportingService.getTicketTrendForecast());
    }

    // 5. Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportingService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}