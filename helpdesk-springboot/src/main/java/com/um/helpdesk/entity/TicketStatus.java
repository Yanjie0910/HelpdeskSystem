package com.um.helpdesk.entity;

public enum TicketStatus {
    NEW,          // user just submitted
    IN_PROGRESS,  // technician/admin is working on it
    COMPLETED     // ticket resolved/closed
}
