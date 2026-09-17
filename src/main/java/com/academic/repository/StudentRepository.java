package com.academic.repository;

import com.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {
    List<Student> findByClassApplyingForAndSection(Integer classId, Integer sectionId);

    Optional<Student> findByIdAndIsDeletedFalse(Integer id);

    List<Student> findByIdInAndStatusAndIsDeletedFalse(List<Integer> studentIds, Integer status);

    Optional<Student> findByAdmissionNoAndIsDeletedFalse(String admissionNo);

    @Query("SELECT s FROM Student s WHERE LOWER(TRIM(s.admissionNo)) IN :admissionNos AND s.isDeleted = false")
    List<Student> findByAdmissionNoInIgnoreCase(@Param("admissionNos") Collection<String> admissionNos);
}
