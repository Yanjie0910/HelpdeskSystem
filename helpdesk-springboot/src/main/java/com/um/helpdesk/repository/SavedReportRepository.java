package com.um.helpdesk.repository;

import com.um.helpdesk.entity.SavedReportArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedReportRepository extends JpaRepository<SavedReportArchive, Long> {
    // Basic CRUD is auto-generated
}