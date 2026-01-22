package com.um.helpdesk;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.*;
import com.um.helpdesk.service.TicketSubmissionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Testing covers the UC14–UC20 :
 *
 * UC14: Create Ticket -> initial status is NEW, generate ticketNumber, and write initial timeline entry.
 *
 * UC15: List tickets by user -> supports status filter (NEW / IN_PROGRESS).
 *
 * UC16: Read ticket details + read timeline.
 *
 * UC17: Feedback submission rules -> ONLY allowed when status is COMPLETED; duplicate submissions are rejected;
 *       after successful submission feedbackSubmitted = true.
 *
 * UC18: Attachment metadata -> persist metadata only (fileName + filePath) and associate it with the ticket.
 *
 * UC19: Keyword search -> search by keyword in description.
 *
 * UC20: Follow-up comment rules -> ONLY allowed when status is IN_PROGRESS; NEW will throw/reject.
 */


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketSubmissionServiceTests {

    @Autowired private TicketSubmissionService ticketSubmissionService;

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketCommentRepository ticketCommentRepository;
    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private FeedbackRepository feedbackRepository;

    private Student studentUser;
    private Department deptIT;

    @BeforeEach
    void setup() {
        Student s = new Student();
        s.setFullName("Lily Tan");
        s.setEmail("lily@test.com");
        s.setPhoneNumber("12345678");
        s.setRole(UserRole.STUDENT);
        s.setActive(true);

        // s.setStudentId("S123");
        // s.setFaculty("FSKTM");
        // s.setProgram("CS");

        studentUser = userRepository.save(s);

        Department d = new Department();
        d.setName("IT");
        deptIT = departmentRepository.save(d);
    }

    // =========================
    // UC14: lodge new ticket + initial timeline
    // =========================
    @Test
    void uc14_lodgeNewTicket_shouldCreateTicketWithNewStatus_andInitialTimeline() {
        Ticket t = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Library System",
                TicketType.COMPLAINT,
                "The library is too slow",
                "12345678",
                "FSKTM",
                deptIT.getId()
        );

        assertNotNull(t.getId());
        assertNotNull(t.getTicketNumber());
        assertEquals(TicketStatus.NEW, t.getStatus());
        assertEquals("Library System", t.getProduct());
        assertEquals(TicketCategory.ACADEMIC, t.getCategory());

        // timeline should have at least 1 record (e.g., "Ticket submitted.")
        List<TicketComment> timeline = ticketSubmissionService.getTicketTimeline(t.getId());
        assertFalse(timeline.isEmpty());
        assertNotNull(timeline.get(0).getMessage());
        assertFalse(timeline.get(0).getMessage().isBlank());
    }

    // =========================
    // UC15: list tickets + filter by status
    // =========================
    @Test
    void uc15_listMyTickets_and_listByStatus_shouldWork() {
        Ticket t1 = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.OTHER,             
                "Laptop",
                TicketType.INQUIRY,
                "Need help",
                null, null,
                deptIT.getId()
        );

        Ticket t2 = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Course Registration",
                TicketType.COMPLAINT,
                "Cannot enroll",
                null, null,
                deptIT.getId()
        );

        // set t2 to IN_PROGRESS
        t2.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(t2);

        List<Ticket> all = ticketSubmissionService.listMyTickets(studentUser.getId());
        assertTrue(all.size() >= 2);

        List<Ticket> onlyNew = ticketSubmissionService.listMyTicketsByStatus(studentUser.getId(), TicketStatus.NEW);
        assertTrue(onlyNew.stream().anyMatch(x -> x.getId().equals(t1.getId())));

        List<Ticket> inProgress = ticketSubmissionService.listMyTicketsByStatus(studentUser.getId(), TicketStatus.IN_PROGRESS);
        assertTrue(inProgress.stream().anyMatch(x -> x.getId().equals(t2.getId())));
    }

    // =========================
    // UC16: ticket details + timeline
    // =========================
    @Test
    void uc16_getTicketDetails_and_getTimeline_shouldWork() {
        Ticket t = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Library System",
                TicketType.COMPLAINT,
                "Library issue",
                null, null,
                deptIT.getId()
        );

        Ticket details = ticketSubmissionService.getTicketDetails(t.getId());
        assertEquals(t.getId(), details.getId());

        List<TicketComment> timeline = ticketSubmissionService.getTicketTimeline(t.getId());
        assertFalse(timeline.isEmpty());
    }

    // =========================
    // UC19: search tickets by keyword in description
    // =========================
    @Test
    void uc19_searchMyTickets_shouldFindByKeywordInDescription() {
        ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Library System",
                TicketType.COMPLAINT,
                "The library website is down",
                null, null,
                deptIT.getId()
        );

        ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.FACILITIES,
                "Classroom",
                TicketType.INQUIRY,
                "Projector not working",
                null, null,
                deptIT.getId()
        );

        List<Ticket> found = ticketSubmissionService.searchMyTickets(studentUser.getId(), "library");
        assertFalse(found.isEmpty());
        assertTrue(found.stream().allMatch(t -> t.getDescription().toLowerCase().contains("library")));
    }

    // =========================
    // UC20: follow-up comment (IN_PROGRESS only)
    // =========================
    @Test
    void uc20_addFollowUp_shouldFailWhenNotInProgress_andSucceedWhenInProgress() {
        Ticket t = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.OTHER,
                "Laptop",
                TicketType.COMPLAINT,
                "Laptop slow",
                null, null,
                deptIT.getId()
        );

        // NEW -> should fail
        assertThrows(IllegalStateException.class, () ->
                ticketSubmissionService.addFollowUpComment(t.getId(), studentUser.getId(), "Please update")
        );

        // set IN_PROGRESS -> should succeed
        t.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(t);

        TicketComment c = ticketSubmissionService.addFollowUpComment(t.getId(), studentUser.getId(), "Any update?");
        assertNotNull(c.getId());
        assertEquals("Any update?", c.getMessage());

        // timeline should now have >= 2 messages
        List<TicketComment> timeline = ticketSubmissionService.getTicketTimeline(t.getId());
        assertTrue(timeline.size() >= 2);
    }

    // =========================
    // UC17: feedback only when COMPLETED + no duplicate
    // =========================
    @Test
    void uc17_submitFeedback_shouldOnlyAllowWhenCompleted_andBlockDuplicate() {
        Ticket t = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Library System",
                TicketType.COMPLAINT,
                "Library issue",
                null, null,
                deptIT.getId()
        );

        // NEW -> should fail
        assertThrows(IllegalStateException.class, () ->
                ticketSubmissionService.submitFeedback(t.getId(), 3, 3, 3, 3, "ok")
        );

        // set COMPLETED -> should succeed
        t.setStatus(TicketStatus.COMPLETED);
        ticketRepository.save(t);

        Feedback f = ticketSubmissionService.submitFeedback(t.getId(), 4, 4, 4, 4, "Good");
        assertNotNull(f.getId());

        // second submit -> should fail
        assertThrows(IllegalStateException.class, () ->
                ticketSubmissionService.submitFeedback(t.getId(), 5, 5, 5, 5, "Again")
        );

        Ticket refreshed = ticketRepository.findById(t.getId()).orElseThrow();
        assertTrue(refreshed.isFeedbackSubmitted());
    }

    // =========================
    // UC14 includes UC17: completed but no feedback -> cannot lodge new
    // =========================
    @Test
    void uc14_shouldRequireFeedbackForPreviousCompletedTicketBeforeNewLodge() {
        Ticket old = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.FACILITIES,
                "Classroom",
                TicketType.COMPLAINT,
                "Old issue",
                null, null,
                deptIT.getId()
        );

        old.setStatus(TicketStatus.COMPLETED);
        old.setFeedbackSubmitted(false);
        ticketRepository.save(old);

        // try lodge new -> should throw
        assertThrows(IllegalStateException.class, () ->
                ticketSubmissionService.lodgeNewTicket(
                        studentUser.getId(),
                        TicketCategory.ACADEMIC,
                        "Library System",
                        TicketType.INQUIRY,
                        "New issue",
                        null, null,
                        deptIT.getId()
                )
        );

        // submit feedback for old -> then lodge new should succeed
        Feedback f = ticketSubmissionService.submitFeedback(old.getId(), 3, 3, 3, 3, "done");
        assertNotNull(f.getId());

        Ticket newer = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Library System",
                TicketType.INQUIRY,
                "New issue",
                null, null,
                deptIT.getId()
        );
        assertNotNull(newer.getId());
    }

    // =========================
    // UC18: add attachment (metadata)
    // =========================
    @Test
    void uc18_addAttachment_shouldSaveFilePath_andLinkToTicket() {
        Ticket t = ticketSubmissionService.lodgeNewTicket(
                studentUser.getId(),
                TicketCategory.ACADEMIC,
                "Library System",
                TicketType.COMPLAINT,
                "Need attachment test",
                null, null,
                deptIT.getId()
        );

        Attachment a = ticketSubmissionService.addAttachment(
                t.getId(),
                "screenshot.png",
                "C:\\temp\\screenshot.png"
        );

        assertNotNull(a.getId());
        assertEquals("screenshot.png", a.getFileName());

        assertEquals("C:\\temp\\screenshot.png", a.getFilePath());

        assertNotNull(a.getTicket());
        assertEquals(t.getId(), a.getTicket().getId());
    }
}
