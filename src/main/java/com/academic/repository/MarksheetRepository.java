package com.academic.repository;

import com.academic.entity.Marksheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarksheetRepository extends JpaRepository<Marksheet, Long> {

    Page<Marksheet> findByIsDeletedFalse(Pageable pageable);

    Page<Marksheet> findByClassIdAndExamTypeIdAndIsDeletedFalse(
            Integer classId,
            Integer examTypeId,
            Pageable pageable);

    java.util.List<Marksheet> findByStudentIdAndIsDeletedFalse(Long studentId);

    java.util.List<Marksheet> findByClassIdAndSessionIdAndIsDeletedFalseOrderByPercentageDesc(Integer classId,
            Integer sessionId);
}
