package com.um.helpdesk.ticketsubmission.launcher;

import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketStatus;
import com.um.helpdesk.service.TicketSubmissionService;
import com.um.helpdesk.ticketsubmission.impl.TicketSubmissionServiceImpl;

import java.util.Scanner;

public class TicketSubmissionConsoleApp {

    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Ticket Submission Console Demo (NO OSGi)");
        System.out.println("==============================================\n");

        TicketSubmissionServiceImpl impl = new TicketSubmissionServiceImpl();
        runMenu(impl);
    }

    private static void runMenu(TicketSubmissionServiceImpl service) {
        Scanner sc = new Scanner(System.in);

        while (running) {
            menu();
            System.out.print("Choose option: ");

            try {
                if (!sc.hasNextInt()) {
                    sc.nextLine();
                    continue;
                }

                int c = sc.nextInt();
                sc.nextLine();

                if (c == 0) break;

                switch (c) {
                    case 1 -> createTicket(service, sc);        // UC14
                    case 2 -> viewAll(service);                 // UC15
                    case 3 -> viewDetails(service, sc);         // UC16
                    case 4 -> updateStatus(service, sc);
                    case 5 -> viewTimeline(service, sc);        // UC16 timeline
                    case 6 -> submitFeedback(service, sc);      // UC17 (4 ratings)
                    case 7 -> followUpCommentPlaceholder();     // UC20 先占位
                    default -> System.out.println("Unknown option.");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println(">>> Ticket Submission Demo exited.");
    }

    private static void menu() {
        System.out.println("\n--- TICKET SUBMISSION MENU ---");
        System.out.println("1. Create Ticket (UC14)");
        System.out.println("2. View All Tickets (UC15)");
        System.out.println("3. View Ticket Details (UC16)");
        System.out.println("4. Update Ticket Status");
        System.out.println("5. View Ticket Timeline (UC16)");
        System.out.println("6. Submit Feedback (UC17 - 4 ratings)");
        System.out.println("7. Add Follow-up Comment (UC20 - TODO)");
        System.out.println("0. Exit");
    }

    // ========== Flows ==========

    private static void createTicket(TicketSubmissionService service, Scanner sc) {
        Ticket t = new Ticket();

        System.out.print("SubmittedByUserId: ");
        Long userId = Long.parseLong(sc.nextLine());
        t.setSubmittedByUserId(userId);

        System.out.print("Title: ");
        t.setTitle(sc.nextLine());

        System.out.print("Category: ");
        t.setCategory(sc.nextLine());

        System.out.print("Description: ");
        t.setDescription(sc.nextLine());

        System.out.print("Location(optional): ");
        t.setLocation(sc.nextLine());

        System.out.print("ContactPhone(optional): ");
        t.setContactPhone(sc.nextLine());

        Ticket saved = service.createTicket(t);
        System.out.println("✓ Ticket created. ID=" + saved.getId() + " Status=" + saved.getStatus());
    }

    private static void viewAll(TicketSubmissionService service) {
        var list = service.getAllTickets();
        if (list.isEmpty()) {
            System.out.println("(no tickets)");
            return;
        }
        list.forEach(t -> System.out.println(
                "#" + t.getId() + " [" + t.getStatus() + "] " + t.getTitle()
                        + " by userId=" + t.getSubmittedByUserId()
                        + (t.isFeedbackSubmitted() ? " (feedback submitted)" : " (no feedback)")
        ));
    }

    private static void viewDetails(TicketSubmissionService service, Scanner sc) {
        System.out.print("TicketId: ");
        Long id = Long.parseLong(sc.nextLine());
        Ticket t = service.getTicketById(id);

        System.out.println("----- TICKET DETAILS -----");
        System.out.println("ID: " + t.getId());
        System.out.println("Status: " + t.getStatus());
        System.out.println("Title: " + t.getTitle());
        System.out.println("Category: " + t.getCategory());
        System.out.println("Description: " + t.getDescription());
        System.out.println("SubmittedByUserId: " + t.getSubmittedByUserId());
        System.out.println("FeedbackSubmitted: " + t.isFeedbackSubmitted());

        // 你 Ticket 里可能没有这些 getter，所以这里只打印最稳的字段
        // 如果你确认 Ticket 有 getFeedbackComment() 再取消注释
        // System.out.println("FeedbackComment: " + t.getFeedbackComment());
    }

    private static void updateStatus(TicketSubmissionService service, Scanner sc) {
        System.out.print("TicketId: ");
        Long id = Long.parseLong(sc.nextLine());

        System.out.print("NewStatus (NEW/IN_PROGRESS/COMPLETED): ");
        TicketStatus st = TicketStatus.valueOf(sc.nextLine().trim().toUpperCase());

        System.out.print("ActorUserId(optional): ");
        String a = sc.nextLine().trim();
        Long actor = a.isBlank() ? null : Long.parseLong(a);

        System.out.print("Note(optional): ");
        String note = sc.nextLine();

        Ticket t = service.updateStatus(id, st, actor, note);
        System.out.println("✓ Updated. Now status=" + t.getStatus());
    }

    private static void viewTimeline(TicketSubmissionService service, Scanner sc) {
        System.out.print("TicketId: ");
        Long id = Long.parseLong(sc.nextLine());

        var tl = service.getTimeline(id);
        if (tl.isEmpty()) {
            System.out.println("(no timeline)");
            return;
        }
        tl.forEach(e -> System.out.println(
                e.getEventAt() + " [" + e.getType() + "] " + e.getMessage()
                        + " (actor=" + e.getActorUserId() + ")"
        ));
    }

    private static void submitFeedback(TicketSubmissionService service, Scanner sc) {
        System.out.print("TicketId: ");
        Long id = Long.parseLong(sc.nextLine());

        // ✅ 你的接口“真实签名”需要 4 个评分
        System.out.print("Timeliness (1-5): ");
        int timeliness = Integer.parseInt(sc.nextLine());

        System.out.print("Professionalism (1-5): ");
        int professionalism = Integer.parseInt(sc.nextLine());

        System.out.print("Communication (1-5): ");
        int communication = Integer.parseInt(sc.nextLine());

        System.out.print("Cooperation (1-5): ");
        int cooperation = Integer.parseInt(sc.nextLine());

        System.out.print("Comment(optional): ");
        String comment = sc.nextLine();

        System.out.print("ActorUserId(optional): ");
        String a = sc.nextLine().trim();
        Long actor = a.isBlank() ? null : Long.parseLong(a);

        Ticket t = service.submitFeedback(id, timeliness, professionalism, communication, cooperation, comment, actor);
        System.out.println("✓ Feedback submitted. Ticket #" + t.getId());
    }

    private static void followUpCommentPlaceholder() {
        System.out.println("UC20 Follow-up comment: TODO (你的 impl 里暂时没有 addComment 方法)");
        System.out.println("下一步我会按你 report 的 UC20，给你补：接口 + impl + timeline 记录。");
    }
}
