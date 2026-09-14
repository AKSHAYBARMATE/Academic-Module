package com.academic.repository;

import com.academic.entity.CollegeCalendarEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeCalendarEventRepository extends JpaRepository<CollegeCalendarEvent, Long> {

    Optional<CollegeCalendarEvent> findByIdAndIsDeletedFalse(Long id);

    List<CollegeCalendarEvent> findByIsDeletedFalseOrderByStartDateAscIdAsc();

    @Query("SELECT e FROM CollegeCalendarEvent e WHERE e.isDeleted = false " +
            "AND (:academicYear IS NULL OR :academicYear = 'all' OR e.academicYear = :academicYear) " +
            "AND (:eventType IS NULL OR :eventType = 'all' OR e.eventType = :eventType) " +
            "AND (:semester IS NULL OR :semester = 'all' OR e.semester = :semester OR e.semester = 'All Semesters') " +
            "AND (:targetProgram IS NULL OR :targetProgram = 'all' OR e.targetProgram = :targetProgram OR e.targetProgram = 'All Programs') " +
            "AND (:status IS NULL OR :status = 'all' OR e.status = :status) " +
            "ORDER BY e.startDate ASC, e.id ASC")
    List<CollegeCalendarEvent> filterEvents(
            @Param("academicYear") String academicYear,
            @Param("eventType") String eventType,
            @Param("semester") String semester,
            @Param("targetProgram") String targetProgram,
            @Param("status") String status
    );

    @Query("SELECT e FROM CollegeCalendarEvent e WHERE e.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(e.targetProgram) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:academicYear IS NULL OR :academicYear = 'all' OR e.academicYear = :academicYear) " +
            "AND (:eventType IS NULL OR :eventType = 'all' OR e.eventType = :eventType) " +
            "AND (:semester IS NULL OR :semester = 'all' OR e.semester = :semester OR e.semester = 'All Semesters') " +
            "AND (:targetProgram IS NULL OR :targetProgram = 'all' OR e.targetProgram = :targetProgram OR e.targetProgram = 'All Programs') " +
            "AND (:status IS NULL OR :status = 'all' OR e.status = :status)")
    Page<CollegeCalendarEvent> searchEvents(
            @Param("search") String search,
            @Param("academicYear") String academicYear,
            @Param("eventType") String eventType,
            @Param("semester") String semester,
            @Param("targetProgram") String targetProgram,
            @Param("status") String status,
            Pageable pageable
    );
}
