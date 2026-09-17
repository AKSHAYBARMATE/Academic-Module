package com.academic.repository;

import com.academic.entity.CollegeMarksheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CollegeMarksheetRepository extends JpaRepository<CollegeMarksheet, Long> {

    Optional<CollegeMarksheet> findByIdAndIsDeletedFalse(Long id);

    Optional<CollegeMarksheet> findByUniversityPrnAndExamSessionAndSemesterAndIsDeletedFalse(
            String universityPrn, String examSession, String semester
    );

    java.util.List<CollegeMarksheet> findByStudentIdIsNullAndIsDeletedFalse();

    @Query("SELECT m FROM CollegeMarksheet m " +
            "WHERE m.isDeleted = false " +
            "AND (:degreeCode IS NULL OR LOWER(m.degreeCode) = LOWER(:degreeCode)) " +
            "AND (:programCode IS NULL OR LOWER(m.programCode) = LOWER(:programCode)) " +
            "AND (:semester IS NULL OR LOWER(m.semester) = LOWER(:semester)) " +
            "AND (:academicYear IS NULL OR LOWER(m.academicYear) = LOWER(:academicYear)) " +
            "AND (:examSession IS NULL OR LOWER(m.examSession) = LOWER(:examSession)) " +
            "AND (:resultStatus IS NULL OR m.resultStatus = :resultStatus) " +
            "AND (:search IS NULL OR " +
            "     LOWER(m.studentName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(m.universityPrn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(m.collegeRollNo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     (m.admissionNo IS NOT NULL AND LOWER(m.admissionNo) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
            "     LOWER(m.universityRollNo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.id DESC")
    Page<CollegeMarksheet> searchAndFilter(
            @Param("degreeCode") String degreeCode,
            @Param("programCode") String programCode,
            @Param("semester") String semester,
            @Param("academicYear") String academicYear,
            @Param("examSession") String examSession,
            @Param("resultStatus") String resultStatus,
            @Param("search") String search,
            Pageable pageable
    );
}
