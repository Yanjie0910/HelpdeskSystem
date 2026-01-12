package com.um.helpdesk.repository;

import com.um.helpdesk.entity.Notification;
import com.um.helpdesk.entity.NotificationStatus;
import com.um.helpdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a specific user
    List<Notification> findByRecipient(User recipient);

    // Find unread notifications for a specific user
    List<Notification> findByRecipientAndIsReadFalse(User recipient);

    // Find notifications by status
    List<Notification> findByStatus(NotificationStatus status);

    // Find notifications by recipient and status
    List<Notification> findByRecipientAndStatus(User recipient, NotificationStatus status);

    // Count unread notifications for a user
    long countByRecipientAndIsReadFalse(User recipient);

    // Find failed notifications (for retry)
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
