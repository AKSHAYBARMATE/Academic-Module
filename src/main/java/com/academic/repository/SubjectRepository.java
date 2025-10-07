package com.academic.repository;


import com.academic.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Page<Subject> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(s.department) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:type IS NULL OR LOWER(s.type) = LOWER(:type)) " +
            "AND (:status IS NULL OR LOWER(s.status) = LOWER(:status)) " +
            "AND (:credits IS NULL OR s.credits = :credits)")
    Page<Subject> searchAndFilter(String search, String type, String status, Integer credits, Pageable pageable);

    Optional<Subject> findByIdAndIsDeletedFalse(Long id);

    boolean existsBySubjectCodeAndIsDeletedFalse(java.lang.String subjectCode);
}
