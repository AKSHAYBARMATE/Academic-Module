package com.academic.repository;

import com.academic.entity.ExamSubjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamSubjectConfigRepository
                extends JpaRepository<ExamSubjectConfig, Long> {

        Optional<ExamSubjectConfig> findBySession_IdAndExamType_IdAndSubject_Id(
                        Integer sessionId,
                        Integer examTypeId,
                        Long subjectId);

        List<ExamSubjectConfig> findBySession_IdAndExamType_IdAndIsDeleteFalse(
                        Integer sessionId,
                        Integer examTypeId);

        @Query("""
                            SELECT e FROM ExamSubjectConfig e
                            WHERE e.isDelete = false
                              AND (:sessionId IS NULL OR e.session.id = :sessionId)
                              AND (:examTypeId IS NULL OR e.examType.id = :examTypeId)
                              AND (:classId IS NULL OR e.classId.id = :classId)
                        """)
        List<ExamSubjectConfig> findAllWithFilters(
                        @Param("sessionId") Integer sessionId,
                        @Param("examTypeId") Integer examTypeId,
                        @Param("classId") Integer classId);

        Optional<ExamSubjectConfig> findBySession_IdAndExamType_IdAndSubject_IdAndClassIdIdAndIsDeleteFalse(Integer id,
                        Integer id1, Long id2, Integer id3);

    Optional<Object> findBySession_IdAndExamType_IdAndSubject_IdAndClassId_IdAndIsDeleteFalse(Integer id, Integer id1, Integer id2, Integer id3);
}
