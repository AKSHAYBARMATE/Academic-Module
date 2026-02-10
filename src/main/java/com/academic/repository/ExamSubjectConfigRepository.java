package com.academic.repository;

import com.academic.entity.ExamSubjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamSubjectConfigRepository
        extends JpaRepository<ExamSubjectConfig, Long> {

    Optional<ExamSubjectConfig>
    findBySession_IdAndExamType_IdAndSubject_Id(
            Integer sessionId,
            Integer examTypeId,
            Long subjectId
    );

    List<ExamSubjectConfig>
    findBySession_IdAndExamType_IdAndIsDeleteFalse(
            Integer sessionId,
            Integer examTypeId
    );

    Optional<ExamSubjectConfig> findBySession_IdAndExamType_IdAndSubject_IdAndClassIdId(Integer id, Integer id1, Long id2, Integer id3);
}
