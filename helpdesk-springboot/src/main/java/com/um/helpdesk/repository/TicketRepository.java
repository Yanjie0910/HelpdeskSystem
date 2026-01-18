package com.um.helpdesk.repository;

import com.um.helpdesk.entity.Ticket;
import com.um.helpdesk.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findBySubmittedById(Long userId);

    List<Ticket> findBySubmittedByIdAndStatus(Long userId, TicketStatus status);

    // Search
    List<Ticket> findBySubmittedByIdAndDescriptionContainingIgnoreCase(Long userId, String keyword);

    List<Ticket> findBySubmittedByIdAndProductContainingIgnoreCase(Long userId, String keyword);
}
