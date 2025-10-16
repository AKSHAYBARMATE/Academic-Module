package com.academic.service;

import com.academic.entity.CommonMaster;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;


import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableServiceImpl implements TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final TimeSlotSubjectMapperRepository mapperRepository;
    private final CommonMasterRepository commonMasterRepository;

    @Override
    @Transactional
    public TimeTableResponse create(TimeTableRequest request) {
        log.info("[{}][{}] Creating timetable: {}", LogContext.getRequestId(), LogContext.getLogId(), request.getTimetableName());

        // Check for duplicate name for the same class and section
        boolean exists = timeTableRepository.existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalse(
                request.getTimetableName(), request.getClassId(), request.getSectionId());
        // Throwing a duplicate exception in your service
        if (timeTableRepository.existsByTimetableNameAndIsDeletedFalse(request.getTimetableName())) {
            throw new CustomException(
                    "Timetable with the same name already exists",
                    "DUPLICATE_RESOURCE",
                    "Timetable name: " + request.getTimetableName()
            );
        }

        // Map request DTO to entity
        TimeTable entity = TimeTable.builder()
                .timetableName(request.getTimetableName())
                .classId(request.getClassId())
                .sectionId(request.getSectionId())
                .daysCoveredId(request.getDaysCoveredId())
                .isDeleted(false)
                .build();

        // Map timeslot DTOs to entity slots
        entity.setSlots(TimeTableMapper.toEntityList(request.getSlots(), entity));

        // Save entity
        TimeTable saved = timeTableRepository.save(entity);

        log.info("[{}][{}] Timetable created with id {}", LogContext.getRequestId(), LogContext.getLogId(), saved.getId());

        // Fetch common master mapping
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        return TimeTableMapper.toResponse(saved, commonMasterMap);
    }

    @Override
    @Transactional(readOnly = true)
    public TimeTableResponse get(Long id) {
        log.info("[{}][{}] Fetching timetable id {}",
                LogContext.getRequestId(),
                LogContext.getLogId(),
                id);

        TimeTable entity = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeTable not found with id: " + id
                ));

        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        return TimeTableMapper.toResponse(entity, commonMasterMap);
    }




    @Override
    @Transactional
    public TimeTableResponse update(Long id, TimeTableRequest request) {
        log.info("[{}][{}] Updating timetable id {}", LogContext.getRequestId(), LogContext.getLogId(), id);

        TimeTable existing = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable not found with id: " + id));

        // Check for duplicate name for another timetable
        boolean exists = timeTableRepository.existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalseAndIdNot(
                request.getTimetableName(), request.getClassId(), request.getSectionId(), id);
        // Throwing a duplicate exception in your service
        if (timeTableRepository.existsByTimetableNameAndIsDeletedFalse(request.getTimetableName())) {
            throw new CustomException(
                    "Timetable with the same name already exists",
                    "DUPLICATE_RESOURCE",
                    "Timetable name: " + request.getTimetableName()
            );
        }

        existing.setTimetableName(request.getTimetableName());
        existing.setClassId(request.getClassId());
        existing.setSectionId(request.getSectionId());
        existing.setDaysCoveredId(request.getDaysCoveredId());

        // Clear old slots and set new ones
        if (existing.getSlots() != null) {
            existing.getSlots().clear();
        }
        existing.setSlots(TimeTableMapper.toEntityList(request.getSlots(), existing));

        TimeTable saved = timeTableRepository.save(existing);

        log.info("[{}][{}] Timetable updated id {}", LogContext.getRequestId(), LogContext.getLogId(), saved.getId());

        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        return TimeTableMapper.toResponse(saved, commonMasterMap);
    }


    @Override
    @Transactional
    public void delete(Long id) {
        log.info("[{}][{}] Soft deleting timetable id {}", LogContext.getRequestId(), LogContext.getLogId(), id);

        TimeTable existing = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable not found with id: " + id));

        existing.setIsDeleted(true);
        timeTableRepository.save(existing);

        log.info("[{}][{}] Timetable soft deleted id {}", LogContext.getRequestId(), LogContext.getLogId(), id);
    }

    @Transactional(readOnly = true)
    @Override
    public StandardResponse<Map<String, Object>> listAll(
            Integer page,
            Integer size,
            Long classId,
            Long section,
            String search) {

        log.info("[{}][{}] Fetching timetables page={}, size={}, classId={}, section={}, search={}",
                LogContext.getRequestId(), LogContext.getLogId(),
                page, size, classId, section, search);

        Pageable pageable = PageRequest.of(
                page != null && page > 0 ? page - 1 : 0,
                size != null && size > 0 ? size : 10,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<TimeTable> timetablePage = timeTableRepository.findAllByFilters(classId, section, search, pageable);

        // Fetch all CommonMaster once for mapping class/section names
        List<CommonMaster> commonMasters = commonMasterRepository.findAll();
        Map<Integer, String> commonMasterMap = commonMasters.stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getCommonMasterKey));

        // Convert entity to response DTO
        List<TimeTableResponse> responseList = timetablePage.getContent().stream()
                .map(tt -> TimeTableMapper.toResponse(tt, commonMasterMap))
                .collect(Collectors.toList());

        // --- Pageable Metadata ---
        Map<String, Object> sortMap = Map.of(
                "empty", timetablePage.getSort().isEmpty(),
                "sorted", timetablePage.getSort().isSorted(),
                "unsorted", timetablePage.getSort().isUnsorted()
        );

        Map<String, Object> pageableMap = Map.of(
                "pageNumber", timetablePage.getNumber(),
                "pageSize", timetablePage.getSize(),
                "sort", sortMap,
                "offset", timetablePage.getPageable().getOffset(),
                "paged", timetablePage.getPageable().isPaged(),
                "unpaged", timetablePage.getPageable().isUnpaged()
        );

        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("content", responseList);
        dataMap.put("pageable", pageableMap);
        dataMap.put("last", timetablePage.isLast());
        dataMap.put("totalPages", timetablePage.getTotalPages());
        dataMap.put("totalElements", timetablePage.getTotalElements());
        dataMap.put("size", timetablePage.getSize());
        dataMap.put("number", timetablePage.getNumber());
        dataMap.put("first", timetablePage.isFirst());
        dataMap.put("numberOfElements", timetablePage.getNumberOfElements());
        dataMap.put("sort", sortMap);
        dataMap.put("empty", timetablePage.isEmpty());

        // Use overloaded success() for Map
        return StandardResponse.success(dataMap, "Fetched timetables successfully");
    }


}


