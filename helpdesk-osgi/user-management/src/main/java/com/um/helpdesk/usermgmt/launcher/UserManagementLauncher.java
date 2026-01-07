package com.um.helpdesk.usermgmt.launcher;

import com.um.helpdesk.usermgmt.Activator;

public class UserManagementLauncher {

    public static void main(String[] args) {
        try {
            System.out.println("==============================================");
            System.out.println("  Starting User Management (OSGi Simulation)");
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
