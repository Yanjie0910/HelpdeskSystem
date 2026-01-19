package com.um.helpdesk.notification.launcher;

import com.um.helpdesk.notification.Activator;

public class NotificationLauncher {

    public static void main(String[] args) {
        try {
            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║  Notification Module Launcher (Standalone Mode)║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println();

            // Create Activator instance
            Activator activator = new Activator();

            // Start bundle logic (simulate OSGi start)
            // Note: Passing null as BundleContext since we're in standalone mode
            activator.start(null);

            // Note: The activator runs its own console demo loop,
            // so this main thread will wait until user exits (option 0)

        } catch (Exception e) {
            System.err.println("!!! ERROR: Failed to start Notification Module");
            e.printStackTrace();
        }
    }
}
