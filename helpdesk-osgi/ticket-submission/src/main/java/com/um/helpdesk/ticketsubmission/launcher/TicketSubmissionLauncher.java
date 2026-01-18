package com.um.helpdesk.ticketsubmission.launcher;

import com.um.helpdesk.ticketsubmission.Activator;

public class TicketSubmissionLauncher {

    public static void main(String[] args) {
        try {
            System.out.println("==============================================");
            System.out.println("  Starting Ticket Submission (Simulation Mode)");
            System.out.println("==============================================\n");

            Activator activator = new Activator();

            // ✅ Simulation mode: context == null -> will run console demo
            activator.start(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
