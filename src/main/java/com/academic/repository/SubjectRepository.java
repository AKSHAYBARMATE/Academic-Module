package com.academic.repository;

import com.academic.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    Optional<Subject> findByIdAndIsDeletedFalse(Integer id);

    default Optional<Subject> findByIdAndIsDeletedFalse(Long id) {
        return id != null ? findByIdAndIsDeletedFalse(id.intValue()) : Optional.empty();
    }

    default Optional<Subject> findById(Long id) {
        return id != null ? findById(id.intValue()) : Optional.empty();
    }

    boolean existsBySubjectCodeAndIsDeletedFalse(String subjectCode);

    boolean existsBySubjectCodeIgnoreCaseAndIsDeletedFalse(String subjectCode);

    boolean existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String subjectCode, Integer id);

    default boolean existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String subjectCode, Long id) {
        return existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(subjectCode, id != null ? id.intValue() : null);
    }

    List<Subject> findByIsDeletedFalseAndStatusOrderBySubjectCodeAsc(String status);

    List<Subject> findByDepartmentIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(String department);

    @Query(value = "SELECT s FROM Subject s " +
            "WHERE s.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR (s.department IS NOT NULL AND LOWER(s.department) LIKE LOWER(CONCAT('%', :search, '%')))) " +
            "AND (:department IS NULL OR LOWER(s.department) = LOWER(:department)) " +
            "AND (:type IS NULL OR LOWER(s.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:credits IS NULL OR s.credits = :credits)",
            countQuery = "SELECT count(s) FROM Subject s " +
            "WHERE s.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR (s.department IS NOT NULL AND LOWER(s.department) LIKE LOWER(CONCAT('%', :search, '%')))) " +
            "AND (:department IS NULL OR LOWER(s.department) = LOWER(:department)) " +
            "AND (:type IS NULL OR LOWER(s.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:credits IS NULL OR s.credits = :credits)")
    Page<Subject> searchAndFilter(
            @Param("search") String search,
            @Param("department") String department,
            @Param("type") String type,
            @Param("status") String status,
            @Param("credits") Integer credits,
            Pageable pageable
    );
}
