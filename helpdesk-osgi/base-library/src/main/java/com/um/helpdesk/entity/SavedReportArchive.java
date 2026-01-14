package com.um.helpdesk.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "saved_reports")
public class SavedReportArchive extends BaseEntity {

    private String reportName;
    private String reportType; // e.g., "Performance", "TicketVolume"
    private String fileFormat; // e.g., "PDF", "CSV"
    private String filePath;

    public SavedReportArchive() {}

    public SavedReportArchive(String reportName, String reportType, String fileFormat, String filePath) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.fileFormat = fileFormat;
        this.filePath = filePath;
    }

    // Getters and Setters
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}