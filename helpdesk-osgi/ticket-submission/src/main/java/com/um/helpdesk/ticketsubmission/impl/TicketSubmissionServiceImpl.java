package com.um.helpdesk.ticketsubmission.impl;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.TicketSubmissionService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TicketSubmissionServiceImpl implements TicketSubmissionService {

    private final Map<Long, Ticket> memTickets = new LinkedHashMap<>();
    private final Map<Long, List<TicketAttachment>> memAttachments = new HashMap<>();

    private final AtomicLong memTicketId = new AtomicLong(1);
    private final AtomicLong memAttachmentId = new AtomicLong(1);

    private Long nextTicketId() { return memTicketId.getAndIncrement(); }
    private Long nextAttachmentId() { return memAttachmentId.getAndIncrement(); }

    // UC14
    @Override
    public Ticket createTicket(Ticket ticket) {
        if (ticket == null) throw new RuntimeException("Ticket cannot be null");
        if (ticket.getSubmittedByUserId() == null) throw new RuntimeException("submittedByUserId is required");
        if (blank(ticket.getTitle())) throw new RuntimeException("title is required");
        if (blank(ticket.getDescription())) throw new RuntimeException("description is required");
        if (blank(ticket.getCategory())) throw new RuntimeException("category is required");
        if (ticket.getType() == null) throw new RuntimeException("ticket type is required (COMPLAINT/INQUIRY/SUGGESTION/COMPLIMENT)");

        // UC14 step2/3 include UC17
        for (Ticket t : memTickets.values()) {
            if (Objects.equals(t.getSubmittedByUserId(), ticket.getSubmittedByUserId())
                    && t.getStatus() == TicketStatus.COMPLETED
                    && !t.isFeedbackSubmitted()) {
                throw new RuntimeException("You must submit feedback for completed ticket #" + t.getId() + " before creating a new ticket.");
            }
        }

        if (ticket.getId() == null) ticket.setId(nextTicketId());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setSubmittedAt(LocalDateTime.now());

        memAttachments.putIfAbsent(ticket.getId(), new ArrayList<>());

        TicketTimelineEntry created = timeline(
                TicketTimelineType.CREATED,
                ticket.getSubmittedByUserId(),
                "Ticket created"
        );
        ticket.getTimeline().add(created);

        memTickets.put(ticket.getId(), ticket);
        return ticket;
    }

    // UC15
    @Override
    public List<Ticket> getAllTickets() {
        return new ArrayList<>(memTickets.values());
    }

    @Override
    public Ticket getTicketById(Long id) {
        if (id == null) throw new RuntimeException("ticketId cannot be null");
        Ticket t = memTickets.get(id);
        if (t == null) throw new RuntimeException("Ticket not found with id: " + id);
        t.setAttachments(getAttachments(id));
        return t;
    }

    @Override
    public List<Ticket> getTicketsByUser(Long userId) {
        if (userId == null) throw new RuntimeException("userId cannot be null");
        List<Ticket> out = new ArrayList<>();
        for (Ticket t : memTickets.values()) {
            if (Objects.equals(t.getSubmittedByUserId(), userId)) {
                t.setAttachments(getAttachments(t.getId()));
                out.add(t);
            }
        }
        return out;
    }

    // UC19 (extend UC15)
    @Override
    public List<Ticket> searchMyTickets(Long userId, String keyword, TicketStatus statusFilter) {
        if (userId == null) throw new RuntimeException("userId cannot be null");

        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<Ticket> all = getTicketsByUser(userId);
        List<Ticket> out = new ArrayList<>();

        for (Ticket t : all) {
            if (statusFilter != null && t.getStatus() != statusFilter) continue;

            if (kw.isEmpty()) {
                out.add(t);
                continue;
            }

            boolean hit =
                    containsIgnoreCase(t.getTitle(), kw) ||
                    containsIgnoreCase(t.getDescription(), kw) ||
                    containsIgnoreCase(t.getCategory(), kw) ||
                    containsIgnoreCase(t.getProduct(), kw) ||
                    containsIgnoreCase(t.getReceiver(), kw);

            if (hit) out.add(t);
        }

        return out;
    }

    // 状态更新（Demo helper / timeline用）
    @Override
    public Ticket updateStatus(Long ticketId, TicketStatus newStatus, Long actorUserId, String note) {
        if (newStatus == null) throw new RuntimeException("newStatus cannot be null");

        Ticket t = getTicketById(ticketId);
        TicketStatus old = t.getStatus();
        t.setStatus(newStatus);

        t.getTimeline().add(
                timeline(
                        TicketTimelineType.STATUS_CHANGED,
                        actorUserId,
                        "Status changed: " + old + " -> " + newStatus + (blank(note) ? "" : (" | " + note))
                )
        );

        memTickets.put(t.getId(), t);
        return t;
    }

    // UC16
    @Override
    public List<TicketTimelineEntry> getTimeline(Long ticketId) {
        Ticket t = getTicketById(ticketId);
        return t.getTimeline() == null ? List.of() : new ArrayList<>(t.getTimeline());
    }

    // UC20 (extend UC16)
    @Override
    public Ticket addFollowUpComment(Long ticketId, Long actorUserId, String message) {
        if (actorUserId == null) throw new RuntimeException("actorUserId is required");
        if (blank(message)) throw new RuntimeException("comment cannot be empty");

        Ticket t = getTicketById(ticketId);

        if (t.getStatus() != TicketStatus.NEW && t.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new RuntimeException("Follow-up comment is only allowed when ticket status is NEW or IN_PROGRESS.");
        }

        t.getTimeline().add(
                timeline(TicketTimelineType.COMMENT_ADDED, actorUserId, message)
        );

        memTickets.put(t.getId(), t);
        return t;
    }

    // UC17
    @Override
    public Ticket submitFeedback(Long ticketId,
                                 int timeliness,
                                 int professionalism,
                                 int communication,
                                 int cooperation,
                                 String comment,
                                 Long actorUserId) {

        Ticket t = getTicketById(ticketId);

        if (t.getStatus() != TicketStatus.COMPLETED) {
            throw new RuntimeException("Feedback can only be submitted for COMPLETED tickets.");
        }

        validateRating("timeliness", timeliness);
        validateRating("professionalism", professionalism);
        validateRating("communication", communication);
        validateRating("cooperation", cooperation);

        t.setRatingTimeliness(timeliness);
        t.setRatingProfessionalism(professionalism);
        t.setRatingCommunication(communication);
        t.setRatingCooperation(cooperation);
        t.setFeedbackComment(comment);
        t.setFeedbackSubmitted(true);

        t.getTimeline().add(
                timeline(
                        TicketTimelineType.FEEDBACK_SUBMITTED,
                        actorUserId,
                        "Feedback submitted: T=" + timeliness
                                + ", P=" + professionalism
                                + ", C=" + communication
                                + ", Coop=" + cooperation
                                + (blank(comment) ? "" : (" | " + comment))
                )
        );

        memTickets.put(t.getId(), t);
        return t;
    }

    // UC18 (extend UC14 / shown in UC16)
    @Override
    public TicketAttachment addAttachment(Long ticketId,
                                          String originalFileName,
                                          String contentType,
                                          long sizeBytes,
                                          Long actorUserId) {

        if (actorUserId == null) throw new RuntimeException("actorUserId is required");
        if (blank(originalFileName)) throw new RuntimeException("originalFileName is required");
        if (blank(contentType)) throw new RuntimeException("contentType is required");
        if (sizeBytes <= 0) throw new RuntimeException("sizeBytes must be > 0");

        Ticket t = getTicketById(ticketId);

        long max = 5L * 1024 * 1024;
        if (sizeBytes > max) throw new RuntimeException("Attachment too large. Max 5MB.");

        List<String> allowed = List.of("image/png", "image/jpeg", "application/pdf", "text/plain");
        if (!allowed.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("Invalid contentType. Allowed: " + allowed);
        }

        TicketAttachment a = new TicketAttachment();
        a.setId(nextAttachmentId());
        a.setTicketId(ticketId);
        a.setOriginalFileName(originalFileName);
        a.setContentType(contentType);
        a.setSizeBytes(sizeBytes);
        a.setUploadedAt(LocalDateTime.now());
        a.setUploadedByUserId(actorUserId);
        a.setStoredFileName("mem://" + ticketId + "/" + a.getId() + "_" + originalFileName);

        memAttachments.putIfAbsent(ticketId, new ArrayList<>());
        memAttachments.get(ticketId).add(a);

        t.getTimeline().add(
                timeline(
                        TicketTimelineType.COMMENT_ADDED,
                        actorUserId,
                        "Attachment uploaded: " + originalFileName + " (" + contentType + ", " + sizeBytes + " bytes)"
                )
        );

        memTickets.put(t.getId(), t);
        return a;
    }

    @Override
    public List<TicketAttachment> getAttachments(Long ticketId) {
        List<TicketAttachment> list = memAttachments.get(ticketId);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }

    private static boolean containsIgnoreCase(String field, String kwLower) {
        if (field == null) return false;
        return field.toLowerCase(Locale.ROOT).contains(kwLower);
    }

    private static void validateRating(String name, int v) {
        if (v < 1 || v > 5) throw new RuntimeException(name + " rating must be between 1 and 5");
    }

    private static TicketTimelineEntry timeline(TicketTimelineType type, Long actorUserId, String message) {
        TicketTimelineEntry e = new TicketTimelineEntry();
        e.setType(type);
        e.setActorUserId(actorUserId);
        e.setEventAt(LocalDateTime.now());
        e.setMessage(message);
        return e;
    }
}
