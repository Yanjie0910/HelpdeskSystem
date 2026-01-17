package com.um.helpdesk.entity;

public enum NotificationType {
    TICKET_SUBMITTED, // When user submits a new ticket
    TICKET_ASSIGNED, // When ticket is assigned to technician
    TICKET_STATUS_CHANGED, // When ticket status changes
    TICKET_RESOLVED, // When ticket is marked as resolved
    TICKET_REOPENED, // When closed ticket is reopened
    TICKET_COMMENTED, // When someone adds a comment to ticket
    REMINDER, // Reminder for overdue tickets
    ESCALATION, // Escalation alert to supervisor/admin
    SYSTEM_ALERT, // General system alerts
    GENERAL // General notifications
}
