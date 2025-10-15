package com.academic.repository;

import com.academic.entity.ExamSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSetupRepository extends JpaRepository<ExamSetup, Long>, JpaSpecificationExecutor<ExamSetup> {

    boolean existsByExamNameAndClassIdAndAcademicYearIdAndIsDeletedFalse(String examName, Integer classId, Integer academicYearId);

    boolean existsByExamNameAndClassIdAndAcademicYearIdAndIsDeletedFalseAndIdNot(String examName, Integer classId, Integer academicYearId, Long id);
}
