package com.um.helpdesk.repository;

import com.um.helpdesk.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByTicket_Id(Long ticketId);
}
