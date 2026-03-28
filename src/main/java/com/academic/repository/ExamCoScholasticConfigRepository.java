package com.academic.repository;

import com.academic.entity.ExamCoScholasticConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamCoScholasticConfigRepository extends JpaRepository<ExamCoScholasticConfig, Integer> {

    Optional<Object> findBySession_IdAndExamType_IdAndClassId_IdAndActivity_IdAndIsDeleteFalse(Integer id, Integer id1, Integer id2, Long id3);

    @Query("""
       SELECT DISTINCT c FROM ExamCoScholasticConfig c
       WHERE c.isDelete = false
         AND (:sessionId IS NULL OR c.session.id = :sessionId)
         AND (:examTypeId IS NULL OR c.examType.id = :examTypeId)
         AND (:classId IS NULL OR c.classId.id = :classId)
       """)
    List<ExamCoScholasticConfig> findAllWithFilters(
            @Param("sessionId") Integer sessionId,
            @Param("examTypeId") Integer examTypeId,
            @Param("classId") Integer classId);
}
