package com.academic.repository;

import com.academic.entity.AcademicCalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

// Spring Data JPA Repository for AcademicCalendarEvent
public interface AcademicCalendarEventRepository extends JpaRepository<AcademicCalendarEvent, Long>, JpaSpecificationExecutor<AcademicCalendarEvent> {

    // Find all non-deleted events, similar to the requested style
    List<AcademicCalendarEvent> findAllByIsDeletedFalse();

    // Find a single non-deleted event by ID
    Optional<AcademicCalendarEvent> findByIdAndIsDeletedFalse(Long id);

    // Example custom query for business logic (e.g., check for duplicates on same date)
    boolean existsByEventNameAndDateAndIsDeletedFalse(String eventName, java.time.LocalDate date);
}
