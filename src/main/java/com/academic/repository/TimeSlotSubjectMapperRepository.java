package com.academic.repository;

import com.academic.entity.TimeSlotSubjectMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeSlotSubjectMapperRepository extends JpaRepository<TimeSlotSubjectMapper, Long> {
    List<TimeSlotSubjectMapper> findByTimeTableId(Long timeTableId);
}
