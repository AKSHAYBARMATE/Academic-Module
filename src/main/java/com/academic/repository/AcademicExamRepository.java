package com.academic.repository;

import com.academic.entity.AcademicExam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicExamRepository extends JpaRepository<AcademicExam, Long>, JpaSpecificationExecutor<AcademicExam> {

    List<AcademicExam> findByIsDeletedFalseOrderByStartDateDesc();

    Optional<AcademicExam> findByIdAndIsDeletedFalse(Long id);

    List<AcademicExam> findByStatusAndIsDeletedFalseOrderByStartDateDesc(String status);

    List<AcademicExam> findByAcademicSessionAndIsDeletedFalseOrderByStartDateDesc(String academicSession);

    @Query("SELECT e FROM AcademicExam e WHERE e.isDeleted = false " +
            "AND (:academicSession IS NULL OR e.academicSession = :academicSession) " +
            "AND (:status IS NULL OR LOWER(e.status) = LOWER(:status)) " +
            "AND (:search IS NULL OR LOWER(e.examName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.examType) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.startDate DESC")
    Page<AcademicExam> searchExams(
            @Param("academicSession") String academicSession,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT e FROM AcademicExam e WHERE e.isDeleted = false " +
            "AND e.startDate <= :date AND e.endDate >= :date " +
            "AND e.status = 'Published'")
    List<AcademicExam> findActivePublishedExamsForDate(@Param("date") LocalDate date);
}

