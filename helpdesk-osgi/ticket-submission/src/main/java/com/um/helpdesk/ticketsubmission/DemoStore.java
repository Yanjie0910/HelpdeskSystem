package com.um.helpdesk.ticketsubmission;

import java.time.LocalDateTime;
import java.util.*;

public final class DemoStore {

    private DemoStore() {}

    // ticketId -> attachments (string paths / filenames)
    private static final Map<Long, List<String>> ATTACHMENTS = new HashMap<>();

    // ticketId -> follow-up comments
    private static final Map<Long, List<CommentEvent>> COMMENTS = new HashMap<>();

    public static void addAttachment(long ticketId, String file) {
        ATTACHMENTS.computeIfAbsent(ticketId, k -> new ArrayList<>()).add(file);
    }

    public static List<String> getAttachments(long ticketId) {
        return ATTACHMENTS.getOrDefault(ticketId, List.of());
    }

    public static void addComment(long ticketId, Long actorUserId, String comment) {
        COMMENTS.computeIfAbsent(ticketId, k -> new ArrayList<>())
                .add(new CommentEvent(LocalDateTime.now(), actorUserId, comment));
    }

    public static List<CommentEvent> getComments(long ticketId) {
        return COMMENTS.getOrDefault(ticketId, List.of());
    }

    public record CommentEvent(LocalDateTime at, Long actorUserId, String message) {}
}
