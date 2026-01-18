package com.um.helpdesk.demo;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.TicketSubmissionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class TicketSubmissionConsoleRunner {

    private final TicketSubmissionService service;

    public TicketSubmissionConsoleRunner(TicketSubmissionService service) {
        this.service = service;
    }

    public void run(Long userId) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n==============================");
            System.out.println("     Ticket Submission Module ");
            System.out.println("==============================");
            System.out.println("1) lodge new helpdesk ticket (UC14)");
            System.out.println("2) view & track my status (UC15)");
            System.out.println("3) view ticket details & communication timeline (UC16)");
            System.out.println("4) submit feedback for completed tickets (UC17)");
            System.out.println("0) back");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> lodgeTicketFlow(sc, userId);          // UC14
                    case "2" -> trackStatusFlow(sc, userId);          // UC15 (+ UC19 extend inside)
                    case "3" -> detailsAndTimelineFlow(sc, userId);   // UC16 (+ UC20 extend inside)
                    case "4" -> submitFeedbackFlow(sc);               // UC17
                    case "0" -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception ex) {
                System.out.println("❌ Error: " + ex.getMessage());
            }
        }
    }

    // ===================== UC14 =====================
    private void lodgeTicketFlow(Scanner sc, Long userId) {
        System.out.println("\n--- lodge new helpdesk ticket (UC14) ---");
        System.out.println("(Note: If you have a completed ticket without feedback, system will require feedback first - UC17 included.)");

        TicketCategory category = askEnum(sc,
                "Category (IT/FINANCE/ACADEMIC/FACILITY/OTHER): ",
                TicketCategory.class);

        System.out.print("Product: ");
        String product = sc.nextLine().trim();

        TicketType type = askEnum(sc,
                "Type (COMPLAINT/INQUIRY/SUGGESTION/COMPLIMENT): ",
                TicketType.class);

        System.out.print("Description: ");
        String desc = sc.nextLine().trim();

        System.out.print("Contact number (optional): ");
        String phone = sc.nextLine().trim();

        System.out.print("Location (optional): ");
        String loc = sc.nextLine().trim();

        System.out.print("Receiver Department ID (optional, press Enter to skip): ");
        String deptStr = sc.nextLine().trim();
        Long deptId = deptStr.isEmpty() ? null : Long.parseLong(deptStr);

        Ticket t = service.lodgeNewTicket(
                userId, category, product, type, desc,
                phone.isEmpty() ? null : phone,
                loc.isEmpty() ? null : loc,
                deptId
        );

        System.out.println("✅ Ticket created!");
        System.out.println("TicketId=" + t.getId() + " | TicketNo=" + t.getTicketNumber() + " | Status=" + t.getStatus());

        // UC18 extend -> UC14
        System.out.print("Do you want to upload attachment now? (Y/N): ");
        String ans = sc.nextLine().trim().toUpperCase();
        if (ans.equals("Y")) {
            uploadAttachmentExtendFlow(sc, t.getId()); // UC18
        }
    }

    // ===================== UC15 =====================
    private void trackStatusFlow(Scanner sc, Long userId) {
        System.out.println("\n--- view & track my status (UC15) ---");
        System.out.println("1) ALL  2) NEW  3) IN_PROGRESS  4) COMPLETED");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();

        List<Ticket> list;
        if ("2".equals(c)) list = service.listMyTicketsByStatus(userId, TicketStatus.NEW);
        else if ("3".equals(c)) list = service.listMyTicketsByStatus(userId, TicketStatus.IN_PROGRESS);
        else if ("4".equals(c)) list = service.listMyTicketsByStatus(userId, TicketStatus.COMPLETED);
        else list = service.listMyTickets(userId);

        printTicketList(list);

        // UC19 extend -> UC15
        System.out.print("\nDo you want to search tickets? (Y/N): ");
        String ans = sc.nextLine().trim().toUpperCase();
        if (ans.equals("Y")) {
            searchTicketsExtendFlow(sc, userId); // UC19
        }
    }

    // ===================== UC16 =====================
    private void detailsAndTimelineFlow(Scanner sc, Long userId) {
        System.out.println("\n--- view ticket details & communication timeline (UC16) ---");
        System.out.print("Enter ticketId: ");
        Long ticketId = Long.parseLong(sc.nextLine().trim());

        Ticket t = service.getTicketDetails(ticketId);
        System.out.println("\nTicketNo: " + t.getTicketNumber());
        System.out.println("Status: " + t.getStatus());
        System.out.println("Category: " + t.getCategory());
        System.out.println("Product: " + t.getProduct());
        System.out.println("Description: " + t.getDescription());

        List<TicketComment> timeline = service.getTicketTimeline(ticketId);
        System.out.println("\n--- Communication Timeline ---");
        if (timeline.isEmpty()) {
            System.out.println("(No comments yet)");
        } else {
            for (TicketComment c : timeline) {
                String authorName = (c.getAuthor() != null) ? c.getAuthor().getFullName() : "N/A";
                Long authorId = (c.getAuthor() != null) ? c.getAuthor().getId() : null;

                System.out.println(
                        c.getCreatedAt()
                                + " | author=" + authorName
                                + (authorId != null ? ("(id=" + authorId + ")") : "")
                                + " | " + c.getMessage()
                );
            }
        }

        // UC20 extend -> UC16
        System.out.print("\nDo you want to add follow-up comment? (Y/N): ");
        String ans = sc.nextLine().trim().toUpperCase();
        if (ans.equals("Y")) {
            addFollowUpExtendFlow(sc, userId, ticketId); // UC20
        }
    }

    // ===================== UC17 =====================
    private void submitFeedbackFlow(Scanner sc) {
        System.out.println("\n--- submit feedback for completed tickets (UC17) ---");
        System.out.print("Enter ticketId: ");
        Long ticketId = Long.parseLong(sc.nextLine().trim());

        int timeliness = askRating(sc, "Timeliness (1-5): ");
        int professionalism = askRating(sc, "Professionalism (1-5): ");
        int communication = askRating(sc, "Communication (1-5): ");
        int cooperation = askRating(sc, "Cooperation (1-5): ");

        System.out.print("Comment (optional): ");
        String comment = sc.nextLine().trim();

        Feedback f = service.submitFeedback(
                ticketId, timeliness, professionalism, communication, cooperation,
                comment.isEmpty() ? null : comment
        );

        System.out.println("✅ Feedback saved! id=" + f.getId());
    }

    // ===================== UC18 (extend -> UC14) =====================
    private void uploadAttachmentExtendFlow(Scanner sc, Long ticketId) {
        System.out.println("\n--- upload attachments (UC18 - extend from UC14) ---");

        System.out.print("File name: ");
        String fileName = sc.nextLine().trim();

        System.out.print("File path/URL: ");
        String filePath = sc.nextLine().trim();

        Attachment a = service.addAttachment(ticketId, fileName, filePath);
        System.out.println("✅ Attachment saved! id=" + a.getId());
    }

    // ===================== UC19 (extend -> UC15) =====================
    private void searchTicketsExtendFlow(Scanner sc, Long userId) {
        System.out.println("\n--- search tickets (UC19 - extend from UC15) ---");
        System.out.print("Keyword (search in description): ");
        String keyword = sc.nextLine().trim();

        List<Ticket> list = service.searchMyTickets(userId, keyword);
        printTicketList(list);
    }

    // ===================== UC20 (extend -> UC16) =====================
    private void addFollowUpExtendFlow(Scanner sc, Long userId, Long ticketId) {
        System.out.println("\n--- add follow-up comment (UC20 - extend from UC16) ---");
        System.out.println("(Only allowed when ticket is IN_PROGRESS)");

        System.out.print("Comment: ");
        String text = sc.nextLine().trim();

        TicketComment c = service.addFollowUpComment(ticketId, userId, text);
        System.out.println("✅ Comment added! id=" + c.getId());
    }

    // ===================== helpers =====================
    private void printTicketList(List<Ticket> list) {
        System.out.println("\n--- Ticket List ---");
        if (list == null || list.isEmpty()) {
            System.out.println("(No tickets)");
            return;
        }
        for (Ticket t : list) {
            System.out.println("[" + t.getId() + "] " + t.getTicketNumber()
                    + " | " + t.getStatus()
                    + " | " + t.getCategory()
                    + " | " + t.getProduct());
        }
    }

    private <E extends Enum<E>> E askEnum(Scanner sc, String prompt, Class<E> enumClass) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim().toUpperCase();
            try {
                return Enum.valueOf(enumClass, raw);
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private int askRating(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= 5) return v;
            } catch (Exception ignored) {}
            System.out.println("Invalid rating. Enter 1-5.");
        }
    }
}
