package com.academic.repository;

import com.academic.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamScheduleRepository
        extends JpaRepository<ExamSchedule, Long> {

    List<ExamSchedule> findBySession_IdAndIsActiveTrue(Integer sessionId);
}
