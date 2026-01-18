package com.um.helpdesk.notification.repository;

import com.um.helpdesk.entity.Notification;
import com.um.helpdesk.entity.NotificationStatus;
import com.um.helpdesk.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class NotificationRepository {

    private final EntityManager em;

    public NotificationRepository(EntityManager em) {
        this.em = em;
    }

    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            em.persist(notification);
            return notification;
        } else {
            return em.merge(notification);
        }
    }

    public Notification findById(Long id) {
        return em.find(Notification.class, id);
    }

    public List<Notification> findAll() {
        TypedQuery<Notification> q = em.createQuery("SELECT n FROM Notification n", Notification.class);
        return q.getResultList();
    }

    public void delete(Notification notification) {
        Notification managed = notification;
        if (!em.contains(notification)) {
            managed = em.merge(notification);
        }
        em.remove(managed);
    }

    public List<Notification> findByRecipient(User recipient) {
        TypedQuery<Notification> q = em.createQuery(
                "SELECT n FROM Notification n WHERE n.recipient = :recipient ORDER BY n.createdAt DESC",
                Notification.class);
        q.setParameter("recipient", recipient);
        return q.getResultList();
    }

    public List<Notification> findUnreadByRecipient(User recipient) {
        TypedQuery<Notification> q = em.createQuery(
                "SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.read = false ORDER BY n.createdAt DESC",
                Notification.class);
        q.setParameter("recipient", recipient);
        return q.getResultList();
    }

    public long countUnreadByRecipient(User recipient) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.read = false",
                Long.class);
        q.setParameter("recipient", recipient);
        return q.getSingleResult();
    }

    public List<Notification> findByStatus(NotificationStatus status) {
        TypedQuery<Notification> q = em.createQuery(
                "SELECT n FROM Notification n WHERE n.status = :status",
                Notification.class);
        q.setParameter("status", status);
        return q.getResultList();
    }

    public List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries) {
        TypedQuery<Notification> q = em.createQuery(
                "SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < :maxRetries",
                Notification.class);
        q.setParameter("status", status);
        q.setParameter("maxRetries", maxRetries);
        return q.getResultList();
    }
}

