package com.um.helpdesk.reporting.controller;

import com.um.helpdesk.entity.SavedReportArchive;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportingController {

    private EntityManager em;

    public ReportingController(EntityManager em) {
        this.em = em;
    }

    public SavedReportArchive createReport(String type, String format) {
        // Logic: In a real system, this would query the TicketService
        // For now, we simulate report generation
        String filename = type + "_" + System.currentTimeMillis() + "." + format.toLowerCase();

        SavedReportArchive report = new SavedReportArchive();
        report.setReportName("Report-" + System.currentTimeMillis());
        report.setReportType(type);
        report.setFileFormat(format);
        report.setFilePath("/var/reports/" + filename);

        em.getTransaction().begin();
        em.persist(report);
        em.getTransaction().commit();

        return report;
    }

    public List<SavedReportArchive> fetchAllReports() {
        TypedQuery<SavedReportArchive> query = em.createQuery("SELECT r FROM SavedReportArchive r", SavedReportArchive.class);
        return query.getResultList();
    }

    public Map<String, Integer> calculateTrends() {
        // Mock analytics logic
        Map<String, Integer> trends = new HashMap<>();
        trends.put("Week 1", 45);
        trends.put("Week 2", 60);
        trends.put("Week 3", 30); // Predicted drop
        return trends;
    }

    public void removeReport(Long id) {
        em.getTransaction().begin();
        SavedReportArchive report = em.find(SavedReportArchive.class, id);
        if (report != null) {
            em.remove(report);
        }
        em.getTransaction().commit();
    }
}