package com.um.helpdesk.reporting.launcher;

import com.um.helpdesk.reporting.Activator;

public class ReportingLauncher {

    public static void main(String[] args) {
        try {
            System.out.println("=================================================");
            System.out.println("   🚀 Reporting Component (Standalone Mode) 🚀   ");
            System.out.println("=================================================\n");

            // 1. Manually create the OSGi Activator
            Activator activator = new Activator();

            // 2. "Start" the bundle manually
            // We pass 'null' because we are not in a real OSGi container.
            // Your Activator code is smart enough to handle (context == null).
            activator.start(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}