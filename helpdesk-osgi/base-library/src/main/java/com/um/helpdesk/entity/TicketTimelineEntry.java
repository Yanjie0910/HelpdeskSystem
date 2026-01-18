package com.um.helpdesk.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_timeline")
public class TicketTimelineEntry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TicketTimelineType type;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Long actorUserId;

    private LocalDateTime eventAt = LocalDateTime.now();

    public TicketTimelineType getType() { return type; }
    public void setType(TicketTimelineType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public LocalDateTime getEventAt() { return eventAt; }
    public void setEventAt(LocalDateTime eventAt) { this.eventAt = eventAt; }
}
