package com.um.helpdesk.service;

import com.um.helpdesk.entity.*;

import java.util.List;

public interface TicketSubmissionService {

    // UC14
    Ticket createTicket(Ticket ticket);

    // UC15
    List<Ticket> getAllTickets();
    Ticket getTicketById(Long id);
    List<Ticket> getTicketsByUser(Long userId);

    // UC19 (search)
    List<Ticket> searchMyTickets(Long userId, String keyword, TicketStatus statusFilter);

    // UC16/UC25 (status changes are part of timeline)
    Ticket updateStatus(Long ticketId, TicketStatus newStatus, Long actorUserId, String note);

    // UC16 timeline
    List<TicketTimelineEntry> getTimeline(Long ticketId);

    // UC20 follow-up comment
    Ticket addFollowUpComment(Long ticketId, Long actorUserId, String message);

    // UC17 feedback (4 ratings)
    Ticket submitFeedback(Long ticketId,
                          int timeliness,
                          int professionalism,
                          int communication,
                          int cooperation,
                          String comment,
                          Long actorUserId);

    // UC18 attachments (Phase 1: metadata only)
    TicketAttachment addAttachment(Long ticketId,
                                   String originalFileName,
                                   String contentType,
                                   long sizeBytes,
                                   Long actorUserId);

    List<TicketAttachment> getAttachments(Long ticketId);
}
