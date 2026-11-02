package com.academic.repository;

import com.academic.entity.AcademicExamSubjectMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicExamSubjectMatrixRepository extends JpaRepository<AcademicExamSubjectMatrix, Long> {

    List<AcademicExamSubjectMatrix> findByExamIdAndIsDeletedFalseOrderBySubjectIdAsc(Long examId);

    List<AcademicExamSubjectMatrix> findByExamIdAndClassIdAndIsDeletedFalseOrderBySubjectIdAsc(Long examId, Long classId);

    Optional<AcademicExamSubjectMatrix> findByExamIdAndSubjectIdAndIsDeletedFalse(Long examId, Long subjectId);

    Optional<AcademicExamSubjectMatrix> findByExamIdAndClassIdAndSubjectIdAndIsDeletedFalse(Long examId, Long classId, Long subjectId);

    Optional<AcademicExamSubjectMatrix> findByIdAndIsDeletedFalse(Long id);
}

