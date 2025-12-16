package com.academic.service;

import com.academic.entity.CommonMaster;
import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.TimeTableMapper;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.TimeSlotSubjectMapperRepository;
import com.academic.repository.TimeTableRepository;
import com.academic.request.TimeTableRequest;
import com.academic.response.LogContext;
import com.academic.response.StandardResponse;
import com.academic.response.TimeTableResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableServiceImpl implements TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final TimeSlotSubjectMapperRepository mapperRepository;
    private final CommonMasterRepository commonMasterRepository;

    @Autowired
    private TimeTableMapper timeTableMapper;

    // ---------------------------------------------------------------------------------------------------
    @Override
    @Transactional
    public TimeTableResponse create(TimeTableRequest request) {
        log.info("[{}][{}] Creating timetable: {}",
                LogContext.getRequestId(), LogContext.getLogId(), request.getTimetableName());

        // Check for duplicate name (unique for name + class + section)
        boolean exists = timeTableRepository.existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalse(
                request.getTimetableName(), request.getClassId(), request.getSectionId());

        if (exists) {
            throw new CustomException(
                    "Timetable with the same name already exists",
                    "DUPLICATE_RESOURCE",
                    "Timetable name: " + request.getTimetableName()
            );
        }

        // Convert request → entity
        TimeTable entity = TimeTable.builder()
                .timetableName(request.getTimetableName())
                .classId(request.getClassId())
                .sectionId(request.getSectionId())
                .daysCoveredId(request.getDaysCoveredId())
                .isDeleted(false)
                .build();

        // Convert slot DTOs → slot entities
        entity.setSlots(timeTableMapper.toEntityList(request.getSlots(), entity));

        // Save parent + slots
        TimeTable saved = timeTableRepository.save(entity);

        // Fetch common master map
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        return timeTableMapper.toResponse(saved, commonMasterMap);
    }

    // ---------------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public TimeTableResponse get(Long id) {
        log.info("[{}][{}] Fetching timetable id {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        TimeTable entity = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable not found with id: " + id));

        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        return timeTableMapper.toResponse(entity, commonMasterMap);
    }

    // ---------------------------------------------------------------------------------------------------
    @Override
    @Transactional
    public TimeTableResponse update(Long id, TimeTableRequest request) {
        log.info("[{}][{}] Updating timetable id {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        TimeTable existing = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable not found with id: " + id));

        // Check duplicate name excluding current timetable
        boolean exists = timeTableRepository.existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalseAndIdNot(
                request.getTimetableName(), request.getClassId(), request.getSectionId(), id);

        if (exists) {
            throw new CustomException(
                    "Timetable with the same name already exists",
                    "DUPLICATE_RESOURCE",
                    "Timetable name: " + request.getTimetableName()
            );
        }

        // Update parent fields
        existing.setTimetableName(request.getTimetableName());
        existing.setClassId(request.getClassId());
        existing.setSectionId(request.getSectionId());
        existing.setDaysCoveredId(request.getDaysCoveredId());

        // Remove old slots (orphan safe)
        if (existing.getSlots() != null) {
            existing.getSlots().clear();
        }

        // Add new slots
        if (request.getSlots() != null && !request.getSlots().isEmpty()) {
            existing.getSlots().addAll(timeTableMapper.toEntityList(request.getSlots(), existing));
        }

        TimeTable saved = timeTableRepository.save(existing);

        // Common master map
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        return timeTableMapper.toResponse(saved, commonMasterMap);
    }

    // ---------------------------------------------------------------------------------------------------
    @Override
    @Transactional
    public void delete(Long id) {
        log.info("[{}][{}] Soft deleting timetable id {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        TimeTable existing = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable not found with id: " + id));

        existing.setIsDeleted(true);
        timeTableRepository.save(existing);

        log.info("[{}][{}] Timetable soft deleted id {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);
    }

    // ---------------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Map<String, Object>> listAll(
            Integer page, Integer size, Long classId, Long section, String search) {

        log.info("[{}][{}] Fetching timetables page={}, size={}, classId={}, section={}, search={}",
                LogContext.getRequestId(), LogContext.getLogId(),
                page, size, classId, section, search);

        Pageable pageable = PageRequest.of(
                page != null && page > 0 ? page - 1 : 0,
                size != null && size > 0 ? size : 10,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<TimeTable> timetablePage = timeTableRepository.findAllByFilters(classId, section, search, pageable);

        // Prepare common master map
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        // Map result to response DTOs
        List<TimeTableResponse> responseList = timetablePage.getContent().stream()
                .map(entity -> timeTableMapper.toResponse(entity, commonMasterMap))
                .collect(Collectors.toList());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("content", responseList);
        metadata.put("totalPages", timetablePage.getTotalPages());
        metadata.put("totalElements", timetablePage.getTotalElements());
        metadata.put("pageNumber", timetablePage.getNumber());
        metadata.put("pageSize", timetablePage.getSize());
        metadata.put("isLast", timetablePage.isLast());
        metadata.put("isFirst", timetablePage.isFirst());

        return StandardResponse.success(metadata, "Fetched timetables successfully");
    }
}
