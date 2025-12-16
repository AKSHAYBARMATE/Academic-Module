package com.academic.mapper;

import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.repository.CommonMasterRepository;
import com.academic.response.TimeSlotResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotMapper {

    @Autowired
    private static CommonMasterRepository commonMasterRepository;

    public static TimeSlotResponse toResponse(TimeSlotSubjectMapper slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .subjectId(slot.getSubjectId())
                .subjectName(commonMasterRepository.findById(Math.toIntExact(slot.getSubjectId())).get().getData())
                .teacherId(slot.getTeacherName())
                .roomId(slot.getRoom())
                .day(slot.getDay())
                .build();
    }

}
