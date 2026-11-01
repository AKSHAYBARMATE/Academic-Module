package com.academic.repository;

import com.academic.entity.ClassEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEventRepository extends JpaRepository<ClassEvent, Long>, JpaSpecificationExecutor<ClassEvent> {

    List<ClassEvent> findByIsDeletedFalseOrderByEventDateAsc();

    Optional<ClassEvent> findByIdAndIsDeletedFalse(Long id);

    /**
     * Check if a specific date has an event applicable to entire school or to a specific class/section.
     */
    @Query(value = "SELECT * FROM class_events " +
            "WHERE is_deleted = false " +
            "AND :date BETWEEN event_date AND IFNULL(end_date, event_date) " +
            "AND (applicable_to = 'ENTIRE_SCHOOL' " +
            "     OR (:classId IS NOT NULL AND JSON_CONTAINS(class_ids, :classIdStr)) " +
            "     OR (:sectionId IS NOT NULL AND JSON_CONTAINS(section_ids, :sectionIdStr))) " +
            "ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<ClassEvent> findEventForDateAndClassSection(
            @Param("date") LocalDate date,
            @Param("classId") Long classId,
            @Param("classIdStr") String classIdStr,
            @Param("sectionId") Long sectionId,
            @Param("sectionIdStr") String sectionIdStr
    );

    /**
     * Fetch all events for a given month and year.
     */
    @Query("SELECT e FROM ClassEvent e WHERE e.isDeleted = false " +
            "AND ((YEAR(e.eventDate) = :year AND MONTH(e.eventDate) = :month) " +
            "  OR (e.endDate IS NOT NULL AND YEAR(e.endDate) = :year AND MONTH(e.endDate) = :month) " +
            "  OR (e.eventDate <= :monthStart AND e.endDate >= :monthEnd)) " +
            "ORDER BY e.eventDate ASC")
    List<ClassEvent> findEventsForMonth(
            @Param("year") int year,
            @Param("month") int month,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );
}

