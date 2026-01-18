package com.um.helpdesk.app.coordinator;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.service.NotificationService;
import com.um.helpdesk.service.ReportingService;
import com.um.helpdesk.service.UserManagementService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ApplicationCoordinator {

    private BundleContext context;

    public ApplicationCoordinator(BundleContext context) {
        this.context = context;
    }

    // --- Helper to get Services safely ---
    private <T> T getService(Class<T> clazz) {
        // 1. Safety Check: If in Simulation Mode (Launcher), context is null.
        if (context == null) {
            return null;
        }

        // 2. Real OSGi Service Lookup
        ServiceReference<T> ref = context.getServiceReference(clazz);
        if (ref != null) {
            return context.getService(ref);
        }
        System.err.println("❌ Service not found in OSGi Registry: " + clazz.getName());
        return null;
    }

    // --- Actions called by UI ---

    public String registerUser(String username, String roleName) {
        // SIMULATION MODE CHECK
        if (context == null) {
            return "⚠️ SIMULATION: Fake User '" + username + "' created (No Database Connection).";
        }

        UserManagementService service = getService(UserManagementService.class);
        if (service != null) {
            try {
                User newUser;
                String normalizedRole = roleName.toUpperCase();

                if (normalizedRole.contains("STUDENT")) {
                    newUser = new Student();
                    ((Student) newUser).setStudentId("S" + System.currentTimeMillis());
                    ((Student) newUser).setFaculty("Computer Science");
                } else if (normalizedRole.contains("STAFF")) {
                    newUser = new Staff();
                } else if (normalizedRole.contains("TECH")) {
                    newUser = new TechnicianSupportStaff();
                } else {
                    newUser = new Student();
                }

                newUser.setFullName(username);
                newUser.setEmail(username.toLowerCase().replaceAll("\\s+", ".") + "@um.edu.my");
                newUser.setPassword("password123");

                try {
                    newUser.setRole(UserRole.valueOf(normalizedRole));
                } catch (IllegalArgumentException e) {
                    newUser.setRole(UserRole.STUDENT);
                }

                service.createUser(newUser);
                return "SUCCESS: User " + username + " created via OSGi Service.";
            } catch (Exception e) {
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }
        return "ERROR: User Service not available.";
    }

    public String generateReport(String type, String format) {
        // SIMULATION MODE CHECK
        if (context == null) {
            return "⚠️ SIMULATION: Generated fake report '" + type + "." + format + "'";
        }

        ReportingService service = getService(ReportingService.class);
        if (service != null) {
            try {
                SavedReportArchive report = service.generateReport(type, format);
                return "SUCCESS: Report generated: " + report.getReportName();
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }
        return "ERROR: Reporting Service not available.";
    }

    public String sendNotification(String msg) {
        // SIMULATION MODE CHECK
        if (context == null) {
            return "⚠️ SIMULATION: Fake notification sent: \"" + msg + "\"";
        }

        NotificationService service = getService(NotificationService.class);
        if (service != null) {
            try {
                Staff dummyRecipient = new Staff();
                dummyRecipient.setId(1L);
                dummyRecipient.setFullName("System Admin");

                Notification notification = new Notification();
                notification.setTitle("System Alert");
                notification.setMessage(msg);
                notification.setRecipient(dummyRecipient);
                notification.setType(NotificationType.REMINDER);
                notification.setPriority(NotificationPriority.HIGH);
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveryChannel(DeliveryChannel.IN_APP);

                Notification saved = service.createNotification(notification);
                service.sendNotification(saved.getId(), DeliveryChannel.IN_APP);

                return "SUCCESS: Notification sent to " + dummyRecipient.getFullName();
            } catch (Exception e) {
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }
        }
        return "ERROR: Notification Service not available.";
    }
}