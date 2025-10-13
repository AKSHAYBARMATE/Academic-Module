package com.academic.mapper;

import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.response.TimeSlotResponse;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotMapper {
    public static TimeSlotResponse toResponse(TimeSlotSubjectMapper slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .subjectId(slot.getSubjectId())
                .teacherId(slot.getTeacherName())
                .roomId(slot.getRoom())
                .build();
    }

}
