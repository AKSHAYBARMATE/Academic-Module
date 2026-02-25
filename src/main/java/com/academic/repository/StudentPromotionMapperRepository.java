package com.academic.repository;

import com.academic.entity.StudentPromotionMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPromotionMapperRepository extends JpaRepository<StudentPromotionMapper, Integer> {

    StudentPromotionMapper findByAcademicYearAndStudentId(Integer academicYear, Integer studentId);

    @Query("SELECT s FROM StudentPromotionMapper s WHERE s.studentId = :studentId AND s.academicYear = :academicYear AND s.status = 1")
    Optional<StudentPromotionMapper> findActivePromotion(Integer studentId, Integer academicYear);

    @Query("SELECT s FROM StudentPromotionMapper s WHERE s.toClass = :toClass AND s.toSection = :toSection AND s.academicYear = :academicYear AND s.status = 1")
    List<StudentPromotionMapper> findActivePromotionsByClassAndSection(Integer toClass, Integer toSection,
            Integer academicYear);
}
