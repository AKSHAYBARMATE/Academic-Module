package com.academic.repository;

import com.academic.entity.AcademicExamStudentMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicExamStudentMarkRepository extends JpaRepository<AcademicExamStudentMark, Long> {

    List<AcademicExamStudentMark> findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalseOrderByRollNoAsc(
            Long examId, Long classId, Long sectionId, Long subjectId);

    Optional<AcademicExamStudentMark> findByExamIdAndClassIdAndSectionIdAndSubjectIdAndStudentIdAndIsDeletedFalse(
            Long examId, Long classId, Long sectionId, Long subjectId, Long studentId);

    List<AcademicExamStudentMark> findByExamIdAndStudentIdAndIsDeletedFalse(Long examId, Long studentId);

    Optional<AcademicExamStudentMark> findByIdAndIsDeletedFalse(Long id);
}

