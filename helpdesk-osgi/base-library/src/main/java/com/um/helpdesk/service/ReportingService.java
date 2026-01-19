package com.um.helpdesk.service;

import com.um.helpdesk.entity.SavedReportArchive;
import java.util.List;
import java.util.Map;

public interface ReportingService {
    // CRUD: Create
    SavedReportArchive generateReport(String reportType, String format);

    // CRUD: Read
    List<SavedReportArchive> getAllSavedReports();

    // CRUD: Delete
    void deleteReport(Long id);

    // CRUD: Update (NEW) - Satisfies Requirement 1
    void updateReport(SavedReportArchive report);

    // Requirement 4: Forecast
    Map<String, Integer> getTicketTrendForecast();

    // Requirement 3: Failure Analysis (NEW)
    Map<String, Object> getFailureRateAnalysis();
}