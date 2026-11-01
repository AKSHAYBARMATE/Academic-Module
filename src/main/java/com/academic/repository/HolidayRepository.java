package com.academic.repository;

import com.academic.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long>, JpaSpecificationExecutor<Holiday> {

    List<Holiday> findByIsDeletedFalseOrderByStartDateAsc();

    Optional<Holiday> findByIdAndIsDeletedFalse(Long id);

    /**
     * Find active holidays overlapping with a date range [startDate, endDate].
     */
    @Query("SELECT h FROM Holiday h WHERE h.isDeleted = false " +
            "AND h.startDate <= :endDate AND h.endDate >= :startDate " +
            "ORDER BY h.startDate ASC")
    List<Holiday> findOverlappingHolidays(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Check if a specific date falls in any holiday for all school or for a specific class.
     */
    @Query(value = "SELECT * FROM holidays " +
            "WHERE is_deleted = false " +
            "AND :date BETWEEN start_date AND end_date " +
            "AND (applicable_to = 'ALL_SCHOOL' OR :classId IS NULL OR JSON_CONTAINS(selected_class_ids, :classIdStr)) " +
            "LIMIT 1", nativeQuery = true)
    Optional<Holiday> findHolidayForDateAndClass(
            @Param("date") LocalDate date,
            @Param("classId") Long classId,
            @Param("classIdStr") String classIdStr
    );

    /**
     * Fetch all holidays for a given month and year.
     */
    @Query("SELECT h FROM Holiday h WHERE h.isDeleted = false " +
            "AND ((YEAR(h.startDate) = :year AND MONTH(h.startDate) = :month) " +
            "  OR (YEAR(h.endDate) = :year AND MONTH(h.endDate) = :month) " +
            "  OR (h.startDate <= :monthStart AND h.endDate >= :monthEnd)) " +
            "ORDER BY h.startDate ASC")
    List<Holiday> findHolidaysForMonth(
            @Param("year") int year,
            @Param("month") int month,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );

    /**
     * Fetch upcoming holidays from a given date.
     */
    @Query("SELECT h FROM Holiday h WHERE h.isDeleted = false " +
            "AND h.endDate >= :today ORDER BY h.startDate ASC")
    List<Holiday> findUpcomingHolidays(@Param("today") LocalDate today);
}

