package com.academic.repository;

import com.academic.entity.AcademicExamScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicExamScheduleSlotRepository extends JpaRepository<AcademicExamScheduleSlot, Long> {

    List<AcademicExamScheduleSlot> findByExamIdAndIsDeletedFalseOrderByExamDateAscStartTimeAsc(Long examId);

    List<AcademicExamScheduleSlot> findByExamIdAndClassIdAndSectionIdAndIsDeletedFalseOrderByExamDateAscStartTimeAsc(
            Long examId, Long classId, Long sectionId);

    Optional<AcademicExamScheduleSlot> findByIdAndIsDeletedFalse(Long id);

    /**
     * Check if a subject is scheduled for this exam, class and section.
     */
    boolean existsByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(
            Long examId, Long classId, Long sectionId, Long subjectId);

    Optional<AcademicExamScheduleSlot> findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(
            Long examId, Long classId, Long sectionId, Long subjectId);

    /**
     * Detect overlapping exams for a class & section on the same date and overlapping time interval.
     */
    @Query("SELECT s FROM AcademicExamScheduleSlot s WHERE s.isDeleted = false " +
            "AND s.classId = :classId AND s.sectionId = :sectionId " +
            "AND s.examDate = :examDate " +
            "AND (:slotId IS NULL OR s.id <> :slotId) " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<AcademicExamScheduleSlot> findConflictingClassSlots(
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("slotId") Long slotId
    );

    /**
     * Detect room conflict for another exam slot at the same room, date, and overlapping time.
     */
    @Query("SELECT s FROM AcademicExamScheduleSlot s WHERE s.isDeleted = false " +
            "AND s.room = :room AND s.room IS NOT NULL AND s.room <> '' " +
            "AND s.examDate = :examDate " +
            "AND (:slotId IS NULL OR s.id <> :slotId) " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<AcademicExamScheduleSlot> findConflictingRoomSlots(
            @Param("room") String room,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("slotId") Long slotId
    );
}

