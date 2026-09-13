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

    List<Subject> findByDepartmentIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(String department);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.department) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.program) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.faculty) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.faculties) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:department IS NULL OR LOWER(s.department) = LOWER(:department)) " +
            "AND (:program IS NULL OR LOWER(s.program) = LOWER(:program)) " +
            "AND (:semester IS NULL OR LOWER(s.semester) = LOWER(:semester)) " +
            "AND (:academicYear IS NULL OR LOWER(s.academicYear) = LOWER(:academicYear)) " +
            "AND (:type IS NULL OR LOWER(s.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:credits IS NULL OR s.credits = :credits)")
    Page<Subject> searchAndFilter(
            @Param("search") String search,
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
