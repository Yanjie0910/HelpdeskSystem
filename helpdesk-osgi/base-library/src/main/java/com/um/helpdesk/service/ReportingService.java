package com.um.helpdesk.service;

import com.um.helpdesk.entity.SavedReportArchive;
import java.util.List;
import java.util.Map;

public interface ReportingService {
    SavedReportArchive generateReport(String reportType, String format);
    List<SavedReportArchive> getAllSavedReports();
    Map<String, Integer> getTicketTrendForecast();
    void deleteReport(Long id);
}