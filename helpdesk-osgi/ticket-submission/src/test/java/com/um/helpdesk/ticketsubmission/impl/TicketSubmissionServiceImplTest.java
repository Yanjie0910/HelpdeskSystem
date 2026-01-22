package com.um.helpdesk.ticketsubmission.impl;

import com.um.helpdesk.entity.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketSubmissionServiceImplTest {

    // ---------- helper: create a valid ticket ----------
    private static Ticket newValidTicket(long userId) {
        Ticket t = new Ticket();
        t.setSubmittedByUserId(userId);
        t.setTitle("Cannot login");
        t.setDescription("Login fails with error");
        t.setCategory("IT");
        t.setReceiver("IT Support");
        t.setType(TicketType.COMPLAINT);
        t.setProduct("WiFi");
        t.setLocation("Library");
        t.setContactPhone("0123456789");
        return t;
    }

    // 1) UC14: createTicket
    @Test
    void createTicket_shouldInitializeStatusTimeAndCreatedTimeline() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Ticket saved = svc.createTicket(newValidTicket(100L));

        assertNotNull(saved.getId());
        assertEquals(TicketStatus.NEW, saved.getStatus());
        assertNotNull(saved.getSubmittedAt());

        // timeline should include CREATED
        List<TicketTimelineEntry> tl = saved.getTimeline();
        assertNotNull(tl);
        assertFalse(tl.isEmpty());
        assertEquals(TicketTimelineType.CREATED, tl.get(0).getType());
        assertEquals("Ticket created", tl.get(0).getMessage());
        assertEquals(100L, tl.get(0).getActorUserId());
    }

    // 2) UC15: getTicketsByUser
    @Test
    void getTicketsByUser_shouldReturnOnlyThatUsersTickets() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        svc.createTicket(newValidTicket(1L));
        svc.createTicket(newValidTicket(1L));
        svc.createTicket(newValidTicket(2L));

        List<Ticket> u1 = svc.getTicketsByUser(1L);
        List<Ticket> u2 = svc.getTicketsByUser(2L);

        assertEquals(2, u1.size());
        assertEquals(1, u2.size());
        assertTrue(u1.stream().allMatch(t -> t.getSubmittedByUserId().equals(1L)));
        assertTrue(u2.stream().allMatch(t -> t.getSubmittedByUserId().equals(2L)));
    }

    // 3) UC19: searchMyTickets (keyword + status filter)
    @Test
    void searchMyTickets_shouldFilterByKeywordAndStatus() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Long userId = 7L;

        // Ticket 1: should match (contains unique keyword + status IN_PROGRESS)
        Ticket t1 = new Ticket();
        t1.setSubmittedByUserId(userId);
        t1.setTitle("Printer issue unique-123");
        t1.setDescription("Cannot print");
        t1.setCategory("IT");
        t1.setReceiver("IT Support");
        t1.setType(TicketType.INQUIRY);
        Ticket saved1 = svc.createTicket(t1);
        svc.updateStatus(saved1.getId(), TicketStatus.IN_PROGRESS, userId, "start");

        // Ticket 2: same status but should NOT match keyword
        Ticket t2 = new Ticket();
        t2.setSubmittedByUserId(userId);
        t2.setTitle("Network problem"); // no "unique-123"
        t2.setDescription("Wifi down");
        t2.setCategory("IT");
        t2.setReceiver("IT Support");
        t2.setType(TicketType.COMPLAINT);
        Ticket saved2 = svc.createTicket(t2);
        svc.updateStatus(saved2.getId(), TicketStatus.IN_PROGRESS, userId, "start");

        // Another user's ticket (should not be returned anyway)
        Ticket t3 = new Ticket();
        t3.setSubmittedByUserId(99L);
        t3.setTitle("Printer issue unique-123");
        t3.setDescription("Other user");
        t3.setCategory("IT");
        t3.setReceiver("IT Support");
        t3.setType(TicketType.INQUIRY);
        svc.createTicket(t3);

        List<Ticket> result = svc.searchMyTickets(userId, "unique-123", TicketStatus.IN_PROGRESS);

        assertEquals(1, result.size());
        assertEquals(saved1.getId(), result.get(0).getId());
    }

    // 4) UC16: updateStatus should add STATUS_CHANGED timeline
    @Test
    void updateStatus_shouldUpdateAndAppendStatusChangedTimeline() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Ticket saved = svc.createTicket(newValidTicket(5L));
        Long id = saved.getId();

        svc.updateStatus(id, TicketStatus.IN_PROGRESS, 777L, "picked up");
        Ticket updated = svc.getTicketById(id);

        assertEquals(TicketStatus.IN_PROGRESS, updated.getStatus());

        List<TicketTimelineEntry> tl = svc.getTimeline(id);
        assertTrue(tl.size() >= 2);

        TicketTimelineEntry last = tl.get(tl.size() - 1);
        assertEquals(TicketTimelineType.STATUS_CHANGED, last.getType());
        assertEquals(777L, last.getActorUserId());
        assertTrue(last.getMessage().contains("NEW -> IN_PROGRESS"));
        assertTrue(last.getMessage().contains("picked up"));
    }

    // 5) UC20: addFollowUpComment allowed only NEW/IN_PROGRESS
    @Test
    void addFollowUpComment_shouldWorkInNewOrInProgress_andWriteTimeline() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Ticket saved = svc.createTicket(newValidTicket(20L));
        Long id = saved.getId();

        svc.addFollowUpComment(id, 20L, "Any updates?");
        List<TicketTimelineEntry> tl = svc.getTimeline(id);

        TicketTimelineEntry last = tl.get(tl.size() - 1);
        assertEquals(TicketTimelineType.COMMENT_ADDED, last.getType());
        assertEquals(20L, last.getActorUserId());
        assertEquals("Any updates?", last.getMessage());

        // now move to COMPLETED, then comment should fail
        svc.updateStatus(id, TicketStatus.COMPLETED, 999L, "done");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.addFollowUpComment(id, 20L, "Thanks"));
        assertTrue(ex.getMessage().toLowerCase().contains("only allowed"));
    }

    // 6) UC17: submitFeedback only when COMPLETED + rating validation + timeline
    @Test
    void submitFeedback_shouldRequireCompleted_andStoreRatingsAndTimeline() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Ticket saved = svc.createTicket(newValidTicket(30L));
        Long id = saved.getId();

        // not completed yet -> should fail
        assertThrows(RuntimeException.class, () ->
                svc.submitFeedback(id, 5, 5, 5, 5, "good", 30L));

        // complete first
        svc.updateStatus(id, TicketStatus.COMPLETED, 999L, "resolved");

        Ticket after = svc.submitFeedback(id, 4, 3, 5, 4, "Good service", 30L);

        assertTrue(after.isFeedbackSubmitted());
        assertEquals(4, after.getRatingTimeliness());
        assertEquals(3, after.getRatingProfessionalism());
        assertEquals(5, after.getRatingCommunication());
        assertEquals(4, after.getRatingCooperation());
        assertEquals("Good service", after.getFeedbackComment());

        List<TicketTimelineEntry> tl = svc.getTimeline(id);
        TicketTimelineEntry last = tl.get(tl.size() - 1);
        assertEquals(TicketTimelineType.FEEDBACK_SUBMITTED, last.getType());
        assertTrue(last.getMessage().contains("Feedback submitted"));
    }

    // 7) UC18: addAttachment + getAttachments (metadata only + constraints)
    @Test
    void addAttachment_shouldStoreMetadata_andAppearInGetAttachments_andWriteTimeline() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Ticket saved = svc.createTicket(newValidTicket(40L));
        Long id = saved.getId();

        TicketAttachment a = svc.addAttachment(
                id,
                "evidence.pdf",
                "application/pdf",
                1024,
                40L
        );

        assertNotNull(a.getId());
        assertEquals(id, a.getTicketId());
        assertEquals("evidence.pdf", a.getOriginalFileName());
        assertEquals("application/pdf", a.getContentType());
        assertEquals(1024, a.getSizeBytes());
        assertNotNull(a.getUploadedAt());
        assertEquals(40L, a.getUploadedByUserId());
        assertTrue(a.getStoredFileName().startsWith("mem://" + id + "/"));

        List<TicketAttachment> list = svc.getAttachments(id);
        assertEquals(1, list.size());
        assertEquals("evidence.pdf", list.get(0).getOriginalFileName());

        // timeline should include attachment upload as COMMENT_ADDED
        List<TicketTimelineEntry> tl = svc.getTimeline(id);
        TicketTimelineEntry last = tl.get(tl.size() - 1);
        assertEquals(TicketTimelineType.COMMENT_ADDED, last.getType());
        assertTrue(last.getMessage().contains("Attachment uploaded"));
    }

    // 8) Extra rule in createTicket: must submit feedback for any completed ticket before creating new ticket
    @Test
    void createTicket_shouldBlockNewTicketIfPreviousCompletedHasNoFeedback() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();

        Ticket t1 = svc.createTicket(newValidTicket(88L));
        svc.updateStatus(t1.getId(), TicketStatus.COMPLETED, 999L, "done");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> svc.createTicket(newValidTicket(88L)));
        assertTrue(ex.getMessage().toLowerCase().contains("must submit feedback"));

        // once feedback submitted, new ticket should be allowed
        svc.submitFeedback(t1.getId(), 5, 5, 5, 5, "ok", 88L);
        Ticket t2 = svc.createTicket(newValidTicket(88L));
        assertNotNull(t2.getId());
    }

    // 9) Attachment constraint: invalid content type should fail
    @Test
    void addAttachment_shouldRejectInvalidContentType() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();
        Ticket saved = svc.createTicket(newValidTicket(50L));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                svc.addAttachment(saved.getId(), "hack.exe", "application/octet-stream", 100, 50L)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("invalid contenttype"));
    }

    // 10) Feedback constraint: rating out of range should fail
    @Test
    void submitFeedback_shouldRejectRatingOutOfRange() {
        TicketSubmissionServiceImpl svc = new TicketSubmissionServiceImpl();
        Ticket saved = svc.createTicket(newValidTicket(60L));
        svc.updateStatus(saved.getId(), TicketStatus.COMPLETED, 999L, "done");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                svc.submitFeedback(saved.getId(), 0, 5, 5, 5, "bad", 60L)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("between 1 and 5"));
    }
}
