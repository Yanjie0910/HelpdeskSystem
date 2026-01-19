package com.um.helpdesk.app.launcher;

public class AppLauncher {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Launching App Component (Simulation Mode)...");

            // NOTE: passing 'null' means no real OSGi services will be found.
            // The UI will open, but buttons might crash if clicked.
            new com.um.helpdesk.app.Activator().start(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}