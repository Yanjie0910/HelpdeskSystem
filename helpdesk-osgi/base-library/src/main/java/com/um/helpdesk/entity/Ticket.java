package com.um.helpdesk.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket extends BaseEntity {

    // UC14
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;      // e.g. IT / Facilities
    private String product;       // UC14 step7
    private String receiver;      // UC14 step7 (e.g. department/role/receiver name)
    private String location;
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    private TicketType type;      // Complaint/Inquiry/Suggestion/Compliment

    private Long submittedByUserId;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.NEW;

    private LocalDateTime submittedAt = LocalDateTime.now();

    // UC17 (expanded rating dimensions)
    private Integer ratingTimeliness;        // 1-5
    private Integer ratingProfessionalism;   // 1-5
    private Integer ratingCommunication;     // 1-5
    private Integer ratingCooperation;       // 1-5
    private String feedbackComment;
    private boolean feedbackSubmitted = false;

    // UC16 / UC18
    @Transient
    private List<TicketAttachment> attachments = new ArrayList<>();

    @Transient
    private List<TicketTimelineEntry> timeline = new ArrayList<>();

    // ---------------- getters/setters ----------------

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public TicketType getType() { return type; }
    public void setType(TicketType type) { this.type = type; }

    public Long getSubmittedByUserId() { return submittedByUserId; }
    public void setSubmittedByUserId(Long submittedByUserId) { this.submittedByUserId = submittedByUserId; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getRatingTimeliness() { return ratingTimeliness; }
    public void setRatingTimeliness(Integer ratingTimeliness) { this.ratingTimeliness = ratingTimeliness; }

    public Integer getRatingProfessionalism() { return ratingProfessionalism; }
    public void setRatingProfessionalism(Integer ratingProfessionalism) { this.ratingProfessionalism = ratingProfessionalism; }

    public Integer getRatingCommunication() { return ratingCommunication; }
    public void setRatingCommunication(Integer ratingCommunication) { this.ratingCommunication = ratingCommunication; }

    public Integer getRatingCooperation() { return ratingCooperation; }
    public void setRatingCooperation(Integer ratingCooperation) { this.ratingCooperation = ratingCooperation; }

    public String getFeedbackComment() { return feedbackComment; }
    public void setFeedbackComment(String feedbackComment) { this.feedbackComment = feedbackComment; }

    public boolean isFeedbackSubmitted() { return feedbackSubmitted; }
    public void setFeedbackSubmitted(boolean feedbackSubmitted) { this.feedbackSubmitted = feedbackSubmitted; }

    public List<TicketAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<TicketAttachment> attachments) { this.attachments = attachments; }

    public List<TicketTimelineEntry> getTimeline() { return timeline; }
    public void setTimeline(List<TicketTimelineEntry> timeline) { this.timeline = timeline; }
}
