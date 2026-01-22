package com.um.helpdesk.service;

import com.um.helpdesk.entity.SavedReportArchive;
import java.util.List;
import java.util.Map;

public interface ReportingService {
    SavedReportArchive generateReport(String reportType, String format);
    List<SavedReportArchive> getAllSavedReports();
    void deleteReport(Long id);
    void updateReport(SavedReportArchive report);
    Map<String, Integer> getTicketTrendForecast();
    Map<String, Object> getFailureRateAnalysis();
}