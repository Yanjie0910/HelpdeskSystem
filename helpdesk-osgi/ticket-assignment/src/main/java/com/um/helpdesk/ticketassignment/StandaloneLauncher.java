package com.um.helpdesk.ticketassignment;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone launcher to run the ticket-assignment module without Karaf
 * Usage: java -jar ticket-assignment.jar
 */
public class StandaloneLauncher {

    private Felix felix;

    public static void main(String[] args) {
        StandaloneLauncher launcher = new StandaloneLauncher();
        try {
            launcher.start();
            launcher.waitForStop();
        } catch (Exception e) {
            System.err.println("Error starting Felix: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        Map<String, Object> config = new HashMap<>();

        // Configure Felix
        config.put(FelixConstants.FRAMEWORK_STORAGE_CLEAN, FelixConstants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        config.put(FelixConstants.FRAMEWORK_STORAGE, "felix-cache");

        // System packages to export (make available to bundles)
        config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            "javax.persistence; version=2.2.0," +
            "javax.persistence.spi; version=2.2.0," +
            "javax.persistence.criteria; version=2.2.0," +
            "javax.persistence.metamodel; version=2.2.0," +
            "javax.sql; version=1.0.0," +
            "org.hibernate; version=5.6.15," +
            "org.hibernate.cfg; version=5.6.15," +
            "org.hibernate.proxy; version=5.6.15," +
            "javassist.util.proxy; version=3.28.0"
        );

        // Create and start Felix
        felix = new Felix(config);
        felix.start();

        BundleContext context = felix.getBundleContext();

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     Felix OSGi Container Started                    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        // Install and start base-library bundle
        String baseLibraryPath = findBundle("base-library");
        if (baseLibraryPath != null) {
            Bundle baseLibrary = context.installBundle("file:" + baseLibraryPath);
            baseLibrary.start();
            System.out.println("✓ Installed: base-library");
        }

        // Install and start ticket-assignment bundle
        String ticketAssignmentPath = findBundle("ticket-assignment");
        if (ticketAssignmentPath != null) {
            Bundle ticketAssignment = context.installBundle("file:" + ticketAssignmentPath);
            ticketAssignment.start();
            System.out.println("✓ Installed: ticket-assignment\n");
        }
    }

    private String findBundle(String bundleName) {
        // Try to find bundle JAR in standard Maven locations
        String[] possiblePaths = {
            "../" + bundleName + "/target/" + bundleName + "-1.0.0.jar",
            "./" + bundleName + "/target/" + bundleName + "-1.0.0.jar",
            "target/" + bundleName + "-1.0.0.jar"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        System.err.println("Warning: Could not find bundle: " + bundleName);
        return null;
    }

    public void waitForStop() throws Exception {
        felix.waitForStop(0);
    }

    public void stop() throws Exception {
        if (felix != null) {
            felix.stop();
        }
    }
}
