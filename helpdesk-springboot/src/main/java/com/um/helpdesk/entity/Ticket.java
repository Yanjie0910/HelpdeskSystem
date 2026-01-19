package com.um.helpdesk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    private String category;

    @ManyToOne
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private User submittedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private TechnicianSupportStaff assignedTo;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department assignedDepartment;

    private LocalDateTime submittedAt = LocalDateTime.now();
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    // For tracking assignment history and chaining
    @ManyToOne
    @JoinColumn(name = "previous_assignee_id")
    private TechnicianSupportStaff previousAssignee;

    private int reassignmentCount = 0;

    // Constructors
    public Ticket() {
    }

    public Ticket(String title, String description, User submittedBy) {
        this.title = title;
        this.description = description;
        this.submittedBy = submittedBy;
        this.submittedAt = LocalDateTime.now();
        this.status = TicketStatus.OPEN;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public TechnicianSupportStaff getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(TechnicianSupportStaff assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Department getAssignedDepartment() {
        return assignedDepartment;
    }

    public void setAssignedDepartment(Department assignedDepartment) {
        this.assignedDepartment = assignedDepartment;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public TechnicianSupportStaff getPreviousAssignee() {
        return previousAssignee;
    }

    public void setPreviousAssignee(TechnicianSupportStaff previousAssignee) {
        this.previousAssignee = previousAssignee;
    }

    public int getReassignmentCount() {
        return reassignmentCount;
    }

    public void setReassignmentCount(int reassignmentCount) {
        this.reassignmentCount = reassignmentCount;
    }
}
