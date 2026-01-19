package com.um.helpdesk.entity;

public enum TicketStatus {
    OPEN,           // Newly submitted, not yet assigned
    ASSIGNED,       // Assigned to a technician
    IN_PROGRESS,    // Technician is working on it
    PENDING,        // Waiting for additional information
    RESOLVED,       // Issue resolved, awaiting closure
    CLOSED,         // Ticket closed
    REOPENED        // Previously resolved/closed but reopened
}
