package com.academic.repository;

import com.academic.entity.AcademicExamMarksSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicExamMarksSubmissionRepository extends JpaRepository<AcademicExamMarksSubmission, Long> {

    Optional<AcademicExamMarksSubmission> findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(
            Long examId, Long classId, Long sectionId, Long subjectId);

    Optional<AcademicExamMarksSubmission> findByIdAndIsDeletedFalse(Long id);
}

