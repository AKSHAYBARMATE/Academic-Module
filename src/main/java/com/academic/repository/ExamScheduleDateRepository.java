package com.academic.repository;

import com.academic.entity.ExamScheduleDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamScheduleDateRepository extends JpaRepository<ExamScheduleDate, Long>, JpaSpecificationExecutor<ExamScheduleDate> {

    List<ExamScheduleDate> findByExamSchedule_IdAndIsDeletedFalse(Long examScheduleId);

    List<ExamScheduleDate> findByClassMaster_IdAndIsDeletedFalse(Integer classId);

    List<ExamScheduleDate> findByExamDateAndIsDeletedFalse(LocalDate examDate);

    List<ExamScheduleDate> findByInvigilator_IdAndIsDeletedFalse(Integer invigilatorId);

    @Query("SELECT e FROM ExamScheduleDate e WHERE e.isDeleted = false " +
            "AND (:examScheduleId IS NULL OR e.examSchedule.id = :examScheduleId) " +
            "AND (:sessionId IS NULL OR e.session.id = :sessionId) " +
            "AND (:classId IS NULL OR e.classMaster.id = :classId) " +
            "AND (:sectionId IS NULL OR e.sectionMaster.id = :sectionId) " +
            "AND (:examDate IS NULL OR e.examDate = :examDate) " +
            "AND (:startDate IS NULL OR e.examDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.examDate <= :endDate) " +
            "ORDER BY e.examDate ASC, e.startTime ASC")
    List<ExamScheduleDate> filterExamSchedules(
            @Param("examScheduleId") Long examScheduleId,
            @Param("sessionId") Integer sessionId,
            @Param("classId") Integer classId,
            @Param("sectionId") Integer sectionId,
            @Param("examDate") LocalDate examDate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
