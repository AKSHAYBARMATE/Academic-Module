package com.academic.repository;

import com.academic.entity.CollegeTimetable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollegeTimetableRepository extends JpaRepository<CollegeTimetable, Long> {

    Optional<CollegeTimetable> findByIdAndIsDeletedFalse(Long id);

    // Class & Section schedule lookup
    List<CollegeTimetable> findByProgramIgnoreCaseAndSemesterIgnoreCaseAndSectionIgnoreCaseAndIsDeletedFalse(
            String program, String semester, String section
    );

    List<CollegeTimetable> findByProgramIgnoreCaseAndSemesterIgnoreCaseAndSectionIgnoreCaseAndAcademicYearIgnoreCaseAndIsDeletedFalse(
            String program, String semester, String section, String academicYear
    );

    // Teacher schedule lookup
    List<CollegeTimetable> findByStaff_IdAndIsDeletedFalse(Integer staffId);

    List<CollegeTimetable> findByStaff_IdAndAcademicYearIgnoreCaseAndIsDeletedFalse(Integer staffId, String academicYear);

    List<CollegeTimetable> findByTeacherNameIgnoreCaseAndAcademicYearIgnoreCaseAndIsDeletedFalse(
            String teacherName, String academicYear
    );

    // Conflict Check: Section slot conflict
    @Query("SELECT c FROM CollegeTimetable c " +
            "WHERE c.isDeleted = false " +
            "AND LOWER(c.program) = LOWER(:program) " +
            "AND LOWER(c.semester) = LOWER(:semester) " +
            "AND LOWER(c.section) = LOWER(:section) " +
            "AND LOWER(c.day) = LOWER(:day) " +
            "AND LOWER(c.timeSlot) = LOWER(:timeSlot) " +
            "AND (:academicYear IS NULL OR c.academicYear IS NULL OR LOWER(c.academicYear) = LOWER(:academicYear)) " +
            "AND (:excludeId IS NULL OR c.id != :excludeId)")
    List<CollegeTimetable> findSectionConflicts(
            @Param("program") String program,
            @Param("semester") String semester,
            @Param("section") String section,
            @Param("day") String day,
            @Param("timeSlot") String timeSlot,
            @Param("academicYear") String academicYear,
            @Param("excludeId") Long excludeId
    );

    // Conflict Check: Teacher overlap conflict
    @Query("SELECT c FROM CollegeTimetable c " +
            "LEFT JOIN c.staff stf " +
            "WHERE c.isDeleted = false " +
            "AND LOWER(c.day) = LOWER(:day) " +
            "AND LOWER(c.timeSlot) = LOWER(:timeSlot) " +
            "AND ((:staffId IS NOT NULL AND stf IS NOT NULL AND stf.id = :staffId) " +
            "     OR (:teacherName IS NOT NULL AND c.teacherName IS NOT NULL AND LOWER(c.teacherName) = LOWER(:teacherName))) " +
            "AND (:academicYear IS NULL OR c.academicYear IS NULL OR LOWER(c.academicYear) = LOWER(:academicYear)) " +
            "AND (:excludeId IS NULL OR c.id != :excludeId)")
    List<CollegeTimetable> findTeacherConflicts(
            @Param("staffId") Integer staffId,
            @Param("teacherName") String teacherName,
            @Param("day") String day,
            @Param("timeSlot") String timeSlot,
            @Param("academicYear") String academicYear,
            @Param("excludeId") Long excludeId
    );

    // Conflict Check: Room overlap conflict
    @Query("SELECT c FROM CollegeTimetable c " +
            "WHERE c.isDeleted = false " +
            "AND LOWER(c.day) = LOWER(:day) " +
            "AND LOWER(c.timeSlot) = LOWER(:timeSlot) " +
            "AND LOWER(c.room) = LOWER(:room) " +
            "AND (:academicYear IS NULL OR c.academicYear IS NULL OR LOWER(c.academicYear) = LOWER(:academicYear)) " +
            "AND (:excludeId IS NULL OR c.id != :excludeId)")
    List<CollegeTimetable> findRoomConflicts(
            @Param("room") String room,
            @Param("day") String day,
            @Param("timeSlot") String timeSlot,
            @Param("academicYear") String academicYear,
            @Param("excludeId") Long excludeId
    );

    // Paginated search and filter query
    @Query(value = "SELECT c FROM CollegeTimetable c " +
            "LEFT JOIN c.subject sub " +
            "LEFT JOIN c.staff stf " +
            "LEFT JOIN c.degree deg " +
            "WHERE c.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(c.program) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(c.section) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(c.room) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR (c.subjectCode IS NOT NULL AND LOWER(c.subjectCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "     OR (c.subjectName IS NOT NULL AND LOWER(c.subjectName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "     OR (c.teacherName IS NOT NULL AND LOWER(c.teacherName) LIKE LOWER(CONCAT('%', :search, '%')))) " +
            "AND (:program IS NULL OR LOWER(c.program) = LOWER(:program)) " +
            "AND (:semester IS NULL OR LOWER(c.semester) = LOWER(:semester)) " +
            "AND (:section IS NULL OR LOWER(c.section) = LOWER(:section)) " +
            "AND (:day IS NULL OR LOWER(c.day) = LOWER(:day)) " +
            "AND (:academicYear IS NULL OR c.academicYear IS NULL OR LOWER(c.academicYear) = LOWER(:academicYear)) " +
            "AND (:staffId IS NULL OR (stf IS NOT NULL AND stf.id = :staffId)) " +
            "AND (:subjectId IS NULL OR (sub IS NOT NULL AND sub.id = :subjectId)) " +
            "AND (:type IS NULL OR LOWER(c.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(c.status) = LOWER(:status))",
            countQuery = "SELECT count(c) FROM CollegeTimetable c " +
            "LEFT JOIN c.subject sub " +
            "LEFT JOIN c.staff stf " +
            "LEFT JOIN c.degree deg " +
            "WHERE c.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(c.program) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(c.section) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(c.room) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR (c.subjectCode IS NOT NULL AND LOWER(c.subjectCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "     OR (c.subjectName IS NOT NULL AND LOWER(c.subjectName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "     OR (c.teacherName IS NOT NULL AND LOWER(c.teacherName) LIKE LOWER(CONCAT('%', :search, '%')))) " +
            "AND (:program IS NULL OR LOWER(c.program) = LOWER(:program)) " +
            "AND (:semester IS NULL OR LOWER(c.semester) = LOWER(:semester)) " +
            "AND (:section IS NULL OR LOWER(c.section) = LOWER(:section)) " +
            "AND (:day IS NULL OR LOWER(c.day) = LOWER(:day)) " +
            "AND (:academicYear IS NULL OR c.academicYear IS NULL OR LOWER(c.academicYear) = LOWER(:academicYear)) " +
            "AND (:staffId IS NULL OR (stf IS NOT NULL AND stf.id = :staffId)) " +
            "AND (:subjectId IS NULL OR (sub IS NOT NULL AND sub.id = :subjectId)) " +
            "AND (:type IS NULL OR LOWER(c.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(c.status) = LOWER(:status))")
    Page<CollegeTimetable> searchAndFilter(
            @Param("search") String search,
            @Param("program") String program,
            @Param("semester") String semester,
            @Param("section") String section,
            @Param("day") String day,
            @Param("academicYear") String academicYear,
            @Param("staffId") Integer staffId,
            @Param("subjectId") Long subjectId,
            @Param("type") String type,
            @Param("status") String status,
            Pageable pageable
    );
}
