package com.um.helpdesk.entity;

import jakarta.persistence.*; // Use jakarta.* for Spring Boot 3
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_reports")
public class SavedReportArchive extends BaseEntity {

    private String reportName;
    private String reportType; // e.g., "Performance", "TicketVolume"
    private String fileFormat; // e.g., "PDF", "CSV"
    private String filePath;

    @Column(name = "generated_date")
    private LocalDateTime generatedDate = LocalDateTime.now();

    public SavedReportArchive() {}

    // Getters and Setters
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
}