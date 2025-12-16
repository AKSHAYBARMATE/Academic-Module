package com.academic.mapper;

import com.academic.entity.CommonMaster;
import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.repository.CommonMasterRepository;
import com.academic.request.TimeSlotDTO;
import com.academic.response.TimeTableResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimeTableMapper {

    @Autowired
    private static CommonMasterRepository commonMasterRepository;

    // Convert TimeTable entity → TimeTableResponse DTO
    public static TimeTableResponse toResponse(TimeTable entity, Map<Integer, String> commonMasterMap) {
        if (entity == null) {
            return null;
        }

        Integer classId = entity.getClassId() != null ? Math.toIntExact(entity.getClassId()) : null;
        Integer sectionId = entity.getSectionId() != null ? Math.toIntExact(entity.getSectionId()) : null;
        Integer daysCoveredId = entity.getDaysCoveredId() != null ? Math.toIntExact(entity.getDaysCoveredId()) : null;

        return TimeTableResponse.builder()
                .id(entity.getId())
                .timetableName(entity.getTimetableName())
                .classId(classId)
                .className(classId != null ? commonMasterMap.get(classId) : null)
                .sectionId(sectionId)
                .sectionName(sectionId != null ? commonMasterMap.get(sectionId) : null)
                .daysCoveredId(daysCoveredId)
                .slots(entity.getSlots() != null
                        ? entity.getSlots().stream()
                        .map(TimeSlotMapper::toResponse)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    // Convert TimeSlotSubjectMapper entity → TimeSlotDTO
    public static TimeSlotDTO toTimeSlotDTO(TimeSlotSubjectMapper slot) {
        if (slot == null) return null;

        return TimeSlotDTO.builder()
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .subjectId(slot.getSubjectId())
                .subjectName(commonMasterRepository.findById(Math.toIntExact(slot.getSubjectId())).get().getData())
                .teacherId(slot.getTeacherName())
                .roomId(slot.getRoom())
                .build();
    }

    // Convert List<TimeSlotDTO> → List<TimeSlotSubjectMapper> with parent TimeTable
    public static List<TimeSlotSubjectMapper> toEntityList(List<TimeSlotDTO> slots, TimeTable parent) {
        if (slots == null) return Collections.emptyList();

        return slots.stream()
                .map(dto -> toEntity(dto, parent))
                .collect(Collectors.toList());
    }

    // Convert single TimeSlotDTO → TimeSlotSubjectMapper entity
    public static TimeSlotSubjectMapper toEntity(TimeSlotDTO dto, TimeTable parent) {
        if (dto == null) return null;

        return TimeSlotSubjectMapper.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .subjectId(dto.getSubjectId())
                .teacherName(dto.getTeacherId())
                .room(dto.getRoomId())
                .day(dto.getDay())
                .timeTable(parent)
                .build();
    }
}
