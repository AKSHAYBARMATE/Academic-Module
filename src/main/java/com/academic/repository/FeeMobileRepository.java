package com.academic.repository;

import com.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FeeMobileRepository extends JpaRepository<Student, Integer> {

    @Query(value = "SELECT * FROM fee_structure WHERE class_id = :classId AND academic_year_id = :academicYearId AND is_deleted = false LIMIT 1", nativeQuery = true)
    Map<String, Object> findFeeStructureByClassAndAcademicYear(@Param("classId") Integer classId,
            @Param("academicYearId") Integer academicYearId);

    @Query(value = "SELECT * FROM fee_payment WHERE student_id = :studentId ORDER BY paid_at DESC", nativeQuery = true)
    List<Map<String, Object>> findPaymentsByStudentId(@Param("studentId") Integer studentId);
}
