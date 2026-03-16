package com.academic.repository;

import com.academic.entity.ExamCoScholasticConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamCoScholasticConfigRepository extends JpaRepository<ExamCoScholasticConfig, Integer> {

    Optional<Object> findBySession_IdAndExamType_IdAndClassId_IdAndActivity_IdAndIsDeleteFalse(Integer id, Integer id1, Integer id2, Long id3);
}
