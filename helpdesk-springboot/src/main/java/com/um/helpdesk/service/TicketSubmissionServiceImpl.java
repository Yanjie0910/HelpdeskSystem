package com.um.helpdesk.service;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TicketSubmissionServiceImpl implements TicketSubmissionService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final AttachmentRepository attachmentRepository;
    private final FeedbackRepository feedbackRepository;

    public TicketSubmissionServiceImpl(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            TicketCommentRepository ticketCommentRepository,
            AttachmentRepository attachmentRepository,
            FeedbackRepository feedbackRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.attachmentRepository = attachmentRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public Ticket lodgeNewTicket(Long userId, TicketCategory category, String product, TicketType type,
                                 String description, String contactNumber, String location, Long receiverDepartmentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // include UC17: 已完成但未feedback 的旧工单要先提交 feedback
        List<Ticket> completed = ticketRepository.findBySubmittedByIdAndStatus(userId, TicketStatus.COMPLETED);
        for (Ticket t : completed) {
            if (!t.isFeedbackSubmitted() && !feedbackRepository.existsByTicket_Id(t.getId())) {
                throw new IllegalStateException(
                        "You must submit feedback for completed ticket before lodging a new ticket. TicketNo=" + t.getTicketNumber()
                );
            }
        }

        Department dept = null;
        if (receiverDepartmentId != null) {
            dept = departmentRepository.findById(receiverDepartmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + receiverDepartmentId));
        }

        Ticket ticket = new Ticket();
        ticket.setSubmittedBy(user);
        ticket.setCategory(category);
        ticket.setProduct(product);
        ticket.setType(type);
        ticket.setDescription(description);
        ticket.setContactNumber(contactNumber);
        ticket.setLocation(location);
        ticket.setReceiverDepartment(dept);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setTicketNumber("T-" + System.currentTimeMillis());

        Ticket saved = ticketRepository.save(ticket);

        // timeline 第一条 (TicketComment: ticket + author)
        TicketComment first = new TicketComment();
        first.setTicket(saved);
        first.setAuthor(user);
        first.setMessage("Ticket submitted.");
        ticketCommentRepository.save(first);

        return saved;
    }

    @Override
    public List<Ticket> listMyTickets(Long userId) {
        return ticketRepository.findBySubmittedById(userId);
    }

    @Override
    public List<Ticket> listMyTicketsByStatus(Long userId, TicketStatus status) {
        return ticketRepository.findBySubmittedByIdAndStatus(userId, status);
    }

    @Override
    public Ticket getTicketDetails(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
    }

    @Override
    public List<TicketComment> getTicketTimeline(Long ticketId) {
        // Repository method is now: findByTicket_IdOrderByCreatedAtAsc
        return ticketCommentRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
    }

    @Override
    public TicketComment addFollowUpComment(Long ticketId, Long userId, String commentText) {
        Ticket t = getTicketDetails(ticketId);
        if (t.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new IllegalStateException("Follow-up comment only allowed when ticket is IN_PROGRESS.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        TicketComment c = new TicketComment();
        c.setTicket(t);
        c.setAuthor(user);
        c.setMessage(commentText);

        return ticketCommentRepository.save(c);
    }

    @Override
    public Attachment addAttachment(Long ticketId, String fileName, String fileUrlOrPath) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }
        if (fileUrlOrPath == null || fileUrlOrPath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path/URL cannot be empty.");
        }

        Attachment a = new Attachment();
        a.setTicket(ticket);
        a.setFileName(fileName.trim());

        a.setFileUrl(fileUrlOrPath.trim());
        a.setFilePath(fileUrlOrPath.trim());

        return attachmentRepository.save(a);
    }

    @Override
    public Feedback submitFeedback(Long ticketId, int timeliness, int professionalism, int communication, int cooperation, String comment) {
        Ticket t = getTicketDetails(ticketId);

        if (t.getStatus() != TicketStatus.COMPLETED) {
            throw new IllegalStateException("Feedback can only be submitted when ticket is COMPLETED.");
        }
        if (t.isFeedbackSubmitted() || feedbackRepository.existsByTicket_Id(ticketId)) {
            throw new IllegalStateException("Feedback already submitted for this ticket.");
        }

        Feedback f = new Feedback();
        f.setTicket(t);
        f.setTimeliness(timeliness);
        f.setProfessionalism(professionalism);
        f.setCommunication(communication);
        f.setCooperation(cooperation);
        f.setComment(comment);

        Feedback saved = feedbackRepository.save(f);

        t.setFeedbackSubmitted(true);
        ticketRepository.save(t);

        return saved;
    }

    @Override
    public List<Ticket> searchMyTickets(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return Collections.emptyList();
        return ticketRepository.findBySubmittedByIdAndDescriptionContainingIgnoreCase(userId, keyword.trim());
    }
}
