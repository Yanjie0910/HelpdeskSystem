package com.um.helpdesk.entity;

public enum NotificationPriority {
    LOW,      // Low priority (e.g., general announcements)
    NORMAL,   // Normal priority (default for most notifications)
    HIGH,     // High priority (e.g., ticket assigned to you)
    URGENT    // Urgent priority (e.g., escalation, overdue ticket)
}
