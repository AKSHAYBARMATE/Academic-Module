package com.academic.mapper;

import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.repository.SubjectRepository;
import com.academic.response.TimeSlotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    @Autowired
    private SubjectRepository subjectRepository;

    public TimeSlotResponse toResponse(TimeSlotSubjectMapper slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .subjectId(slot.getSubjectId())
                .subjectName(subjectRepository.findById(slot.getSubjectId())
                        .orElseThrow(() -> new RuntimeException("Subject not found"))
                        .getSubjectName())
                .teacherId(slot.getTeacherName())
                .roomId(slot.getRoom())
                .day(slot.getDay())
                .build();
    }

}
