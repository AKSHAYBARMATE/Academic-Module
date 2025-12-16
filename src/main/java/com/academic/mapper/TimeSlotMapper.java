package com.academic.mapper;

import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.repository.CommonMasterRepository;
import com.academic.response.TimeSlotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TimeSlotMapper {

    @Autowired
    private  CommonMasterRepository commonMasterRepository;

    public TimeSlotResponse toResponse(TimeSlotSubjectMapper slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .subjectId(slot.getSubjectId())
                .subjectName(commonMasterRepository.findById(Math.toIntExact(slot.getSubjectId()))
                        .orElseThrow(() -> new RuntimeException("Subject not found"))
                        .getData())
                .teacherId(slot.getTeacherName())
                .roomId(slot.getRoom())
                .day(slot.getDay())
                .build();
    }

}
