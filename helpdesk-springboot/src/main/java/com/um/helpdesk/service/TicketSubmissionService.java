package com.um.helpdesk.service;

import com.um.helpdesk.entity.*;

import java.util.List;

public interface TicketSubmissionService {

    // UC14 lodge new helpdesk ticket (include UC17)
    Ticket lodgeNewTicket(Long userId, TicketCategory category, String product, TicketType type,
                          String description, String contactNumber, String location, Long receiverDepartmentId);

    // UC15 view & track status
    List<Ticket> listMyTickets(Long userId);
    List<Ticket> listMyTicketsByStatus(Long userId, TicketStatus status);

    // UC16 view ticket details & communication timeline
    Ticket getTicketDetails(Long ticketId);
    List<TicketComment> getTicketTimeline(Long ticketId);

    // UC20 add follow-up comment (inprogress only)
    TicketComment addFollowUpComment(Long ticketId, Long userId, String commentText);

    // UC18 upload attachments (metadata)
    Attachment addAttachment(Long ticketId, String fileName, String fileUrlOrPath);

    // UC17 submit feedback for completed tickets
    Feedback submitFeedback(Long ticketId, int timeliness, int professionalism, int communication, int cooperation, String comment);

    // UC19 search tickets
    List<Ticket> searchMyTickets(Long userId, String keyword);
}
