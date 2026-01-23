package com.um.helpdesk.ticketassignment.launcher;

import com.um.helpdesk.ticketassignment.Activator;

public class TicketAssignmentLauncher {

    public static void main(String[] args) {
        try {
            System.out.println("==============================================");
            System.out.println("  Starting Ticket Assignment (OSGi Simulation)");
            System.out.println("==============================================\n");

            // Create Activator manually
            Activator activator = new Activator();

            // Start bundle logic (simulate OSGi)
            activator.start(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
