package com.um.helpdesk.entity;

public enum NotificationStatus {
    PENDING,              // Notification created but not sent yet
    SENT,                 // Notification sent to delivery channel
    DELIVERED,            // Notification successfully delivered
    READ,                 // User has read the notification
    FAILED,               // Delivery failed (will retry)
    QUEUED_FOR_RETRY,     // Failed delivery, queued for retry
    PERMANENTLY_FAILED    // Max retries exceeded, permanently failed
}
