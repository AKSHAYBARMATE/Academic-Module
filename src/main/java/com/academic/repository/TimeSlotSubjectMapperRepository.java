package com.academic.repository;

import com.academic.entity.TimeSlotSubjectMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimeSlotSubjectMapperRepository extends JpaRepository<TimeSlotSubjectMapper, Long> {
    List<TimeSlotSubjectMapper> findByTimeTableId(Long timeTableId);

    List<TimeSlotSubjectMapper> findByTeacherId(Long teacherId);

    /**
     * Find existing slot assignments for a teacher that conflict with the given
     * day + time window across all non-deleted timetables.
     * Used during timetable creation / update to prevent double-booking a teacher.
     */
    @Query("""
    SELECT s
    FROM TimeSlotSubjectMapper s
    JOIN s.timeTable t
    WHERE t.isDeleted = false
      AND s.teacherId = :teacherId
      AND s.day = :day
      AND s.startTime < :endTime
      AND s.endTime > :startTime
    """)
    List<TimeSlotSubjectMapper> findConflictingSlots(
            @Param("teacherId") Long teacherId,
            @Param("day") Integer day,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );

}
