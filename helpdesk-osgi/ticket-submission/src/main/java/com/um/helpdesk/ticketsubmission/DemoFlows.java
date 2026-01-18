package com.um.helpdesk.ticketsubmission;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.TicketSubmissionService;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class DemoFlows {
    public static void uc14_lodgeNewTicket(TicketSubmissionService service, Scanner sc) {
        System.out.println();
        System.out.println("[UC14] Lodge New Helpdesk Ticket");

        Ticket t = new Ticket();

        System.out.print("SubmittedByUserId: ");
        Long userId = readLong(sc, "SubmittedByUserId");
        t.setSubmittedByUserId(userId);

        System.out.print("Title: ");
        t.setTitle(readNonBlank(sc, "Title"));

        // Category + Receiver combined (ONE selection)
        CategoryReceiver cr = selectCategoryReceiver(sc);
        t.setCategory(cr.category);
        t.setReceiver(cr.receiver);

        // Type (ONE selection)
        t.setType(selectTicketType(sc));

        System.out.print("Description: ");
        t.setDescription(readNonBlank(sc, "Description"));

        System.out.print("ContactPhone(optional): ");
        t.setContactPhone(sc.nextLine().trim());

        System.out.print("Location(optional): ");
        t.setLocation(sc.nextLine().trim());

        // Product is OPTIONAL free-text (no selection menu)
        System.out.print("Product(optional): ");
        t.setProduct(sc.nextLine().trim());

        Ticket saved = service.createTicket(t);
        System.out.println("✓ Ticket created. ID=" + saved.getId() + " Status=" + saved.getStatus());

        // UC18 extends UC14: ask ONCE only
        boolean upload = askYesNo(sc, "Upload attachment now for Ticket #" + saved.getId() + "? (y/n): ");
        if (upload) {
            uc18_uploadAttachment(service, sc, saved.getId(), userId);
        }
    }

    // =========================
    // UC15 View & Track My Ticket Status (extends UC19)
    // =========================
    public static void uc15_viewAndTrack(TicketSubmissionService service, Scanner sc) {
        System.out.println();
        System.out.println("[UC15] View & Track My Ticket Status");

        System.out.print("MyUserId: ");
        Long userId = readLong(sc, "MyUserId");

        List<Ticket> all = service.getTicketsByUser(userId);
        if (all.isEmpty()) {
            System.out.println("E1: No tickets found.");
            return;
        }

        System.out.println("--- NEW ---");
        printByStatus(all, TicketStatus.NEW);

        System.out.println("--- IN_PROGRESS ---");
        printByStatus(all, TicketStatus.IN_PROGRESS);

        System.out.println("--- COMPLETED ---");
        printByStatus(all, TicketStatus.COMPLETED);

        // UC19 extends UC15
        if (askYesNo(sc, "Search my tickets? (y/n): ")) {
            uc19_searchTickets(service, sc, userId);
        }
    }

    // =========================
    // UC16 View Ticket Details & Timeline (extends UC20; shows UC18)
    // =========================
    public static void uc16_viewDetailsAndTimeline(TicketSubmissionService service, Scanner sc) {
        System.out.println();
        System.out.println("[UC16] View Ticket Details & Communication Timeline");

        System.out.print("TicketId: ");
        Long id = readLong(sc, "TicketId");

        Ticket t = service.getTicketById(id);

        System.out.println("----- TICKET DETAILS -----");
        System.out.println("ID: " + t.getId());
        System.out.println("Status: " + t.getStatus());
        System.out.println("Type: " + t.getType());
        System.out.println("Title: " + t.getTitle());
        System.out.println("Category: " + t.getCategory());
        System.out.println("Receiver: " + t.getReceiver());
        System.out.println("Product: " + t.getProduct());
        System.out.println("Description: " + t.getDescription());
        System.out.println("SubmittedByUserId: " + t.getSubmittedByUserId());
        System.out.println("FeedbackSubmitted: " + t.isFeedbackSubmitted());

        System.out.println("----- ATTACHMENTS (UC18) -----");
        List<TicketAttachment> att = service.getAttachments(id);
        if (att.isEmpty()) {
            System.out.println("(none)");
        } else {
            att.forEach(a -> System.out.println("- " + a.getOriginalFileName()
                    + " (" + a.getContentType() + ", " + a.getSizeBytes() + " bytes)"));
        }

        System.out.println("----- TIMELINE -----");
        var tl = service.getTimeline(id);
        if (tl.isEmpty()) {
            System.out.println("(no timeline)");
        } else {
            tl.forEach(e -> System.out.println(
                    e.getEventAt() + " [" + e.getType() + "] " + e.getMessage()
                            + " (actor=" + e.getActorUserId() + ")"
            ));
        }

        // UC20 extends UC16
        if (askYesNo(sc, "Add follow-up comment? (y/n): ")) {
            uc20_addFollowUpComment(service, sc, id);
        }
    }

    // =========================
    // UC17 Submit Feedback
    // =========================
    public static void uc17_submitFeedback(TicketSubmissionService service, Scanner sc) {
        System.out.println();
        System.out.println("[UC17] Submit Feedback for Completed Tickets");

        System.out.print("TicketId: ");
        Long id = readLong(sc, "TicketId");

        System.out.print("ActorUserId (usually same as submittedByUserId): ");
        Long actorId = readLong(sc, "ActorUserId");

        int timeliness = readRating(sc, "Timeliness (1-5): ");
        int professionalism = readRating(sc, "Professionalism (1-5): ");
        int communication = readRating(sc, "Communication (1-5): ");
        int cooperation = readRating(sc, "Cooperation (1-5): ");

        System.out.print("Comment(optional): ");
        String cmt = sc.nextLine().trim();

        Ticket t = service.submitFeedback(id, timeliness, professionalism, communication, cooperation, cmt, actorId);
        System.out.println("✓ Feedback submitted. Ticket #" + t.getId());
    }

    // =========================
    // Demo helper: Update status (to create scenarios)
    // =========================
    public static void demo_updateStatus(TicketSubmissionService service, Scanner sc) {
        System.out.println();
        System.out.println("(Demo Helper) Update Ticket Status");

        System.out.print("TicketId: ");
        Long id = readLong(sc, "TicketId");

        System.out.print("NewStatus (NEW/IN_PROGRESS/COMPLETED): ");
        TicketStatus st = TicketStatus.valueOf(readNonBlank(sc, "NewStatus").toUpperCase(Locale.ROOT));

        System.out.print("ActorUserId(optional): ");
        String a = sc.nextLine().trim();
        Long actor = a.isBlank() ? null : Long.parseLong(a);

        System.out.print("Note(optional): ");
        String note = sc.nextLine();

        Ticket t = service.updateStatus(id, st, actor, note);
        System.out.println("✓ Updated. Now status=" + t.getStatus());
    }

    // =========================
    // UC18 Upload Attachment (metadata only)
    // =========================
    private static void uc18_uploadAttachment(TicketSubmissionService service, Scanner sc, Long ticketId, Long actorUserId) {
        System.out.println();
        System.out.println("[UC18] Upload Attachments");

        System.out.print("OriginalFileName: ");
        String name = readNonBlank(sc, "OriginalFileName");

        System.out.print("ContentType (image/png, image/jpeg, application/pdf, text/plain): ");
        String ct = readNonBlank(sc, "ContentType");

        System.out.print("SizeBytes: ");
        long size = readLong(sc, "SizeBytes");

        TicketAttachment a = service.addAttachment(ticketId, name, ct, size, actorUserId);
        System.out.println("✓ Attachment uploaded. ID=" + a.getId());
    }

    // =========================
    // UC19 Search Tickets (extends UC15)
    // =========================
    private static void uc19_searchTickets(TicketSubmissionService service, Scanner sc, Long userId) {
        System.out.println();
        System.out.println("[UC19] Search Tickets");

        System.out.print("Keyword: ");
        String kw = sc.nextLine().trim();

        System.out.print("StatusFilter(optional: NEW/IN_PROGRESS/COMPLETED, blank=all): ");
        String st = sc.nextLine().trim();
        TicketStatus filter = st.isBlank() ? null : TicketStatus.valueOf(st.toUpperCase(Locale.ROOT));

        var list = service.searchMyTickets(userId, kw, filter);
        if (list.isEmpty()) {
            System.out.println("E1: No matching tickets found.");
            return;
        }
        list.forEach(t -> System.out.println(
                "#" + t.getId() + " [" + t.getStatus() + "] "
                        + t.getTitle() + " | " + t.getCategory() + " | " + t.getReceiver()
        ));
    }

    // =========================
    // UC20 Add Follow-up Comment (extends UC16)
    // =========================
    private static void uc20_addFollowUpComment(TicketSubmissionService service, Scanner sc, Long ticketId) {
        System.out.println();
        System.out.println("[UC20] Add Follow-up Comment");

        System.out.print("ActorUserId: ");
        Long actorId = readLong(sc, "ActorUserId");

        System.out.print("Message: ");
        String msg = readNonBlank(sc, "Message");

        Ticket t = service.addFollowUpComment(ticketId, actorId, msg);
        System.out.println("✓ Comment added. Ticket #" + t.getId());
    }

    // =========================
    // Category + Receiver combined menu (single choice)
    // =========================
    private static CategoryReceiver selectCategoryReceiver(Scanner sc) {
        while (true) {
            System.out.println("Select Receiver:");
            System.out.println("1. IT Support");
            System.out.println("2. Facilities Office");
            System.out.println("3. Academic Office");
            System.out.println("4. Finance Office");
            System.out.print("Choose (1-4): ");

            String s = sc.nextLine().trim();
            switch (s) {
                case "1": return new CategoryReceiver("IT", "IT Support");
                case "2": return new CategoryReceiver("Facility", "Facilities Office");
                case "3": return new CategoryReceiver("Academic", "Academic Office");
                case "4": return new CategoryReceiver("Finance", "Finance Office");
                default:
                    System.out.println("Invalid choice. Please choose 1-4.");
            }
        }
    }

    private static TicketType selectTicketType(Scanner sc) {
        while (true) {
            System.out.print("Type (1=COMPLAINT, 2=INQUIRY, 3=SUGGESTION, 4=COMPLIMENT): ");
            String s = sc.nextLine().trim();
            switch (s) {
                case "1": return TicketType.COMPLAINT;
                case "2": return TicketType.INQUIRY;
                case "3": return TicketType.SUGGESTION;
                case "4": return TicketType.COMPLIMENT;
                default:
                    System.out.println("Invalid type. Choose 1-4.");
            }
        }
    }

    // =========================
    // helpers
    // =========================
    private static boolean askYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Please enter y/n.");
        }
    }

    private static String readNonBlank(Scanner sc, String field) {
        while (true) {
            String s = sc.nextLine();
            if (s != null && !s.trim().isEmpty()) return s.trim();
            System.out.print(field + " cannot be empty. Re-enter: ");
        }
    }

    private static Long readLong(Scanner sc, String field) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                System.out.print(field + " must be a number. Re-enter: ");
            }
        }
    }

    private static int readRating(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= 5) return v;
            } catch (Exception ignored) {}
            System.out.println("Rating must be 1-5.");
        }
    }

    private static void printByStatus(List<Ticket> all, TicketStatus st) {
        all.stream()
                .filter(t -> t.getStatus() == st)
                .forEach(t -> System.out.println(
                        "#" + t.getId()
                                + " | " + t.getTitle()
                                + " | submittedAt=" + t.getSubmittedAt()
                ));
    }

    private static class CategoryReceiver {
        final String category;
        final String receiver;
        CategoryReceiver(String category, String receiver) {
            this.category = category;
            this.receiver = receiver;
        }
    }
}
