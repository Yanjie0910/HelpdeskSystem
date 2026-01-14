package com.um.helpdesk.reporting.impl;

import com.um.helpdesk.entity.SavedReportArchive;
import com.um.helpdesk.service.ReportingService;
import com.um.helpdesk.reporting.controller.ReportingController;

import java.util.List;
import java.util.Map;

public class ReportingServiceImpl implements ReportingService {

    private final ReportingController controller;

    public ReportingServiceImpl(ReportingController controller) {
        this.controller = controller;
    }

    @Override
    public SavedReportArchive generateReport(String reportType, String format) {
        return controller.createReport(reportType, format);
    }

    @Override
    public List<SavedReportArchive> getAllSavedReports() {
        return controller.fetchAllReports();
    }

    @Override
    public Map<String, Integer> getTicketTrendForecast() {
        return controller.calculateTrends();
    }

    @Override
    public void deleteReport(Long id) {
        controller.removeReport(id);
    }
}