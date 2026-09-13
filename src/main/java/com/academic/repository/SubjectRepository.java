package com.academic.repository;

import com.academic.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByIdAndIsDeletedFalse(Long id);

    default Optional<Subject> findByIdAndIsDeletedFalse(Integer id) {
        return id != null ? findByIdAndIsDeletedFalse(id.longValue()) : Optional.empty();
    }

    boolean existsBySubjectCodeAndIsDeletedFalse(String subjectCode);

    boolean existsBySubjectCodeIgnoreCaseAndIsDeletedFalse(String subjectCode);

    boolean existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String subjectCode, Long id);

    default boolean existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String subjectCode, Integer id) {
        return existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(subjectCode, id != null ? id.longValue() : null);
    }

    List<Subject> findByIsDeletedFalseAndStatusOrderBySubjectCodeAsc(String status);

    List<Subject> findByProgramIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(String program);

    List<Subject> findByDepartment_NameIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(String departmentName);

    default List<Subject> findByDepartmentIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(String department) {
        return findByDepartment_NameIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(department);
    }

    List<Subject> findByDepartment_IdAndIsDeletedFalseOrderBySubjectCodeAsc(Integer departmentId);

    List<Subject> findByDegree_IdAndIsDeletedFalseOrderBySubjectCodeAsc(Integer degreeId);

    @Query(value = "SELECT s FROM Subject s " +
            "LEFT JOIN s.department dept " +
            "LEFT JOIN s.degree deg " +
            "WHERE s.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR (dept IS NOT NULL AND LOWER(dept.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "     OR (deg IS NOT NULL AND (LOWER(deg.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(deg.name) LIKE LOWER(CONCAT('%', :search, '%')))) " +
            "     OR LOWER(s.program) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.faculty) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.faculties) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:departmentId IS NULL OR (dept IS NOT NULL AND dept.id = :departmentId)) " +
            "AND (:degreeId IS NULL OR (deg IS NOT NULL AND deg.id = :degreeId)) " +
            "AND (:department IS NULL OR (dept IS NOT NULL AND LOWER(dept.name) = LOWER(:department))) " +
            "AND (:program IS NULL OR LOWER(s.program) = LOWER(:program)) " +
            "AND (:semester IS NULL OR LOWER(s.semester) = LOWER(:semester)) " +
            "AND (:academicYear IS NULL OR s.academicYear IS NULL OR LOWER(s.academicYear) = LOWER(:academicYear)) " +
            "AND (:type IS NULL OR LOWER(s.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:credits IS NULL OR s.credits = :credits)",
            countQuery = "SELECT count(s) FROM Subject s " +
            "LEFT JOIN s.department dept " +
            "LEFT JOIN s.degree deg " +
            "WHERE s.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR (dept IS NOT NULL AND LOWER(dept.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "     OR (deg IS NOT NULL AND (LOWER(deg.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(deg.name) LIKE LOWER(CONCAT('%', :search, '%')))) " +
            "     OR LOWER(s.program) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.faculty) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.faculties) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:departmentId IS NULL OR (dept IS NOT NULL AND dept.id = :departmentId)) " +
            "AND (:degreeId IS NULL OR (deg IS NOT NULL AND deg.id = :degreeId)) " +
            "AND (:department IS NULL OR (dept IS NOT NULL AND LOWER(dept.name) = LOWER(:department))) " +
            "AND (:program IS NULL OR LOWER(s.program) = LOWER(:program)) " +
            "AND (:semester IS NULL OR LOWER(s.semester) = LOWER(:semester)) " +
            "AND (:academicYear IS NULL OR s.academicYear IS NULL OR LOWER(s.academicYear) = LOWER(:academicYear)) " +
            "AND (:type IS NULL OR LOWER(s.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:credits IS NULL OR s.credits = :credits)")
    Page<Subject> searchAndFilter(
            @Param("search") String search,
            @Param("departmentId") Integer departmentId,
            @Param("degreeId") Integer degreeId,
            @Param("department") String department,
            @Param("program") String program,
            @Param("semester") String semester,
            @Param("academicYear") String academicYear,
            @Param("type") String type,
            @Param("status") String status,
            @Param("credits") Integer credits,
            Pageable pageable
    );
}
