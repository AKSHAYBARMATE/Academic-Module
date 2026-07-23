package com.academic.service;

import com.academic.entity.ProxyAssignment;
import com.academic.entity.ProxyTemplate;
import com.academic.entity.Staff;
import com.academic.entity.Subject;
import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.exception.ResourceNotFoundException;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.ProxyAssignmentRepository;
import com.academic.repository.ProxyTemplateRepository;
import com.academic.repository.StaffRepository;
import com.academic.repository.SubjectRepository;
import com.academic.repository.TimeSlotSubjectMapperRepository;
import com.academic.repository.TimeTableRepository;
import com.academic.request.ProxyAssignmentRequest;
import com.academic.request.ProxyTemplateRequest;
import com.academic.request.TimeSlotDTO;
import com.academic.response.LogContext;
import com.academic.response.ProxyAssignmentResponse;
import com.academic.response.ProxyTemplateResponse;
import com.academic.response.StandardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyServiceImpl implements ProxyService {

    private static final String[] DAY_NAMES = {"", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"};

    private final ProxyTemplateRepository templateRepository;
    private final ProxyAssignmentRepository assignmentRepository;
    private final TimeTableRepository timetableRepository;
    private final TimeSlotSubjectMapperRepository slotRepository;
    private final StaffRepository staffRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final SubjectRepository subjectRepository;

    // =========================================================================
    //  PROXY TEMPLATE MANAGEMENT
    // =========================================================================

    @Override
    @Transactional
    public StandardResponse<ProxyTemplateResponse> createTemplate(ProxyTemplateRequest request) {
        log.info("[{}][{}] Creating Proxy Template: {}",
                LogContext.getRequestId(), LogContext.getLogId(), request.getTemplateName());

        try {
            // Resolve substitute teacher name if not set
            String subTeacherName = request.getSubstituteTeacherName();
            if (subTeacherName == null || subTeacherName.isBlank()) {
                Staff staff = staffRepository.findById(request.getSubstituteTeacherId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Substitute teacher staff not found with ID: " + request.getSubstituteTeacherId()));
                subTeacherName = staff.getFirstName() + " " + staff.getLastName();
            }

            ProxyTemplate template = ProxyTemplate.builder()
                    .templateName(request.getTemplateName())
                    .substituteTeacherId(request.getSubstituteTeacherId())
                    .substituteTeacherName(subTeacherName)
                    .remarks(request.getRemarks())
                    .isDeleted(false)
                    .build();

            ProxyTemplate saved = templateRepository.save(template);
            return StandardResponse.success(toResponse(saved), "Proxy template created successfully.");

        } catch (ResourceNotFoundException ex) {
            return StandardResponse.error(ex.getMessage(), "NOT_FOUND", ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to create proxy template", ex);
            return StandardResponse.error(
                    "Failed to create proxy template.", "INTERNAL_SERVER_ERROR", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public StandardResponse<ProxyTemplateResponse> updateTemplate(Long id, ProxyTemplateRequest request) {
        log.info("[{}][{}] Updating Proxy Template ID: {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        try {
            ProxyTemplate existing = templateRepository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Proxy template not found with ID: " + id));

            String subTeacherName = request.getSubstituteTeacherName();
            if (subTeacherName == null || subTeacherName.isBlank() || 
                    !existing.getSubstituteTeacherId().equals(request.getSubstituteTeacherId())) {
                Staff staff = staffRepository.findById(request.getSubstituteTeacherId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Substitute teacher staff not found with ID: " + request.getSubstituteTeacherId()));
                subTeacherName = staff.getFirstName() + " " + staff.getLastName();
            }

            existing.setTemplateName(request.getTemplateName());
            existing.setSubstituteTeacherId(request.getSubstituteTeacherId());
            existing.setSubstituteTeacherName(subTeacherName);
            existing.setRemarks(request.getRemarks());

            ProxyTemplate updated = templateRepository.save(existing);
            return StandardResponse.success(toResponse(updated), "Proxy template updated successfully.");

        } catch (ResourceNotFoundException ex) {
            return StandardResponse.error(ex.getMessage(), "NOT_FOUND", ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to update proxy template", ex);
            return StandardResponse.error(
                    "Failed to update proxy template.", "INTERNAL_SERVER_ERROR", ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ProxyTemplateResponse> getTemplateById(Long id) {
        log.info("[{}][{}] Fetching Proxy Template ID: {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        ProxyTemplate template = templateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proxy template not found with ID: " + id));

        return StandardResponse.success(toResponse(template), "Proxy template fetched successfully.");
    }

    @Override
    @Transactional
    public StandardResponse<Void> deleteTemplate(Long id) {
        log.info("[{}][{}] Soft-deleting Proxy Template ID: {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        ProxyTemplate template = templateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proxy template not found with ID: " + id));

        template.setIsDeleted(true);
        templateRepository.save(template);

        return StandardResponse.success("Proxy template deleted successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Map<String, Object>> listTemplates(Integer page, Integer size, String search) {
        log.info("[{}][{}] Listing Proxy Templates: page={}, size={}, search={}",
                LogContext.getRequestId(), LogContext.getLogId(), page, size, search);

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0) ? size : 10;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<ProxyTemplate> result = templateRepository.findAllTemplates(search, pageable);

        List<ProxyTemplateResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("content", content);
        metadata.put("totalPages", result.getTotalPages());
        metadata.put("totalElements", result.getTotalElements());
        metadata.put("pageNumber", result.getNumber() + 1);
        metadata.put("pageSize", result.getSize());
        metadata.put("isLast", result.isLast());
        metadata.put("isFirst", result.isFirst());

        return StandardResponse.success(metadata, "Proxy templates list fetched successfully.");
    }

    // =========================================================================
    //  PROXY ASSIGNMENT SCHEDULE
    // =========================================================================

    @Override
    @Transactional
    public StandardResponse<ProxyAssignmentResponse> assignProxySlot(ProxyAssignmentRequest request) {
        log.info("[{}][{}] Assigning Proxy Slot: templateId={}, slotId={}, date={}",
                LogContext.getRequestId(), LogContext.getLogId(),
                request.getTemplateId(), request.getSlotId(), request.getProxyDate());

        try {
            // 1. Validate template exists
            ProxyTemplate template = templateRepository.findByIdAndIsDeletedFalse(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Proxy template not found with ID: " + request.getTemplateId()));

            // 2. Validate timetable exists
            TimeTable timetable = timetableRepository.findByIdAndIsDeletedFalse(request.getTimetableId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Timetable not found with ID: " + request.getTimetableId()));

            // 3. Validate slot exists and belongs to the timetable
            TimeSlotSubjectMapper slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Timetable slot not found with ID: " + request.getSlotId()));

            if (!slot.getTimeTable().getId().equals(request.getTimetableId())) {
                return StandardResponse.error(
                        "The selected slot does not belong to the specified timetable.",
                        "SLOT_TIMETABLE_MISMATCH",
                        "slotId",
                        "Slot ID: " + request.getSlotId());
            }

            // 4. Check duplicate: same slot already has a proxy on the same date
            boolean duplicateProxy = assignmentRepository
                    .existsBySlotIdAndProxyDateAndIsDeletedFalse(
                            request.getSlotId(), request.getProxyDate());
            if (duplicateProxy) {
                return StandardResponse.error(
                        "A proxy is already assigned for this slot on " + request.getProxyDate() + ".",
                        "DUPLICATE_PROXY",
                        "slotId",
                        "Duplicate assignment for slot " + request.getSlotId() + " on date " + request.getProxyDate());
            }

            // 5. Resolve original teacher name & ID

            String originalTeacherName = request.getOriginalTeacherName();

            // 6. Resolve subject/class/section contexts
            Long subjectId = request.getSubjectId() != null ? request.getSubjectId() : slot.getSubjectId();
            String subjectName = request.getSubjectName();
            if (subjectName == null || subjectName.isBlank()) {
                if (subjectId != null) {
                    subjectName = subjectRepository.findById(subjectId)
                            .map(Subject::getSubjectName)
                            .orElse("Unknown");
                }
            }

            Long classId = request.getClassId() != null ? request.getClassId() : timetable.getClassId();
            Long sectionId = request.getSectionId() != null ? request.getSectionId() : timetable.getSectionId();

            ProxyAssignment assignment = ProxyAssignment.builder()
                    .template(template)
                    .timetableId(request.getTimetableId())
                    .slotId(request.getSlotId())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .dayOfWeek(request.getDayOfWeek())
                    .proxyDate(request.getProxyDate())
                    .originalTeacherName(originalTeacherName)
                    .subjectId(subjectId)
                    .subjectName(subjectName)
                    .classId(classId)
                    .sectionId(sectionId)
                    .remarks(request.getRemarks())
                    .isClassTeacher(request.getIsClassTeacher())
                    .status("ACTIVE")
                    .isDeleted(false)
                    .build();

            ProxyAssignment saved = assignmentRepository.save(assignment);
            Map<Integer, String> cmMap = buildCommonMasterMap();
            ProxyAssignmentResponse response = toResponse(saved, timetable, cmMap);

            return StandardResponse.success(response, "Proxy assignment generated successfully.");

        } catch (ResourceNotFoundException ex) {
            return StandardResponse.error(ex.getMessage(), "NOT_FOUND", ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to assign proxy slot", ex);
            return StandardResponse.error(
                    "Failed to assign proxy slot.", "INTERNAL_SERVER_ERROR", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public StandardResponse<Void> deleteAssignment(Long id) {
        log.info("[{}][{}] Soft-deleting Proxy Assignment ID: {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        ProxyAssignment assignment = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proxy assignment not found with ID: " + id));

        assignment.setIsDeleted(true);
        assignmentRepository.save(assignment);

        return StandardResponse.success("Proxy assignment deleted successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Map<String, Object>> listAssignments(
            Integer page, Integer size,
            Long templateId, Long timetableId, String proxyDateStr, String status) {

        log.info("[{}][{}] Listing Proxy Assignments: page={}, size={}, templateId={}, timetableId={}, date={}, status={}",
                LogContext.getRequestId(), LogContext.getLogId(), page, size, templateId, timetableId, proxyDateStr, status);

        LocalDate proxyDate = null;
        if (proxyDateStr != null && !proxyDateStr.isBlank()) {
            try {
                proxyDate = LocalDate.parse(proxyDateStr);
            } catch (DateTimeParseException e) {
                return StandardResponse.error(
                        "Invalid proxyDate format (expected yyyy-MM-dd).", "INVALID_DATE", "proxyDate", e.getMessage());
            }
        }

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0) ? size : 10;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<ProxyAssignment> result = assignmentRepository.findAllAssignments(
                templateId, timetableId, proxyDate, status, pageable);

        Map<Integer, String> cmMap = buildCommonMasterMap();
        Set<Long> ttIds = result.getContent().stream()
                .map(ProxyAssignment::getTimetableId)
                .collect(Collectors.toSet());
        Map<Long, TimeTable> timetableMap = timetableRepository.findAllById(ttIds)
                .stream()
                .collect(Collectors.toMap(TimeTable::getId, t -> t));

        List<ProxyAssignmentResponse> content = result.getContent().stream()
                .map(pa -> toResponse(pa, timetableMap.get(pa.getTimetableId()), cmMap))
                .collect(Collectors.toList());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("content", content);
        metadata.put("totalPages", result.getTotalPages());
        metadata.put("totalElements", result.getTotalElements());
        metadata.put("pageNumber", result.getNumber() + 1);
        metadata.put("pageSize", result.getSize());
        metadata.put("isLast", result.isLast());
        metadata.put("isFirst", result.isFirst());

        return StandardResponse.success(metadata, "Proxy assignments list fetched successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProxyAssignmentResponse>> getAssignmentsForTeacher(Long teacherId) {
        log.info("[{}][{}] Fetching Proxy Assignments for Teacher ID: {}",
                LogContext.getRequestId(), LogContext.getLogId(), teacherId);

        List<ProxyAssignment> assignments = assignmentRepository
                .findByTemplateSubstituteTeacherIdAndIsDeletedFalse(teacherId);

        Map<Integer, String> cmMap = buildCommonMasterMap();
        Set<Long> ttIds = assignments.stream()
                .map(ProxyAssignment::getTimetableId)
                .collect(Collectors.toSet());
        Map<Long, TimeTable> timetableMap = timetableRepository.findAllById(ttIds)
                .stream()
                .collect(Collectors.toMap(TimeTable::getId, t -> t));

        List<ProxyAssignmentResponse> responseList = assignments.stream()
                .map(pa -> toResponse(pa, timetableMap.get(pa.getTimetableId()), cmMap))
                .collect(Collectors.toList());

        return StandardResponse.success(responseList, "Proxy Assignments fetched successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<TimeSlotDTO>> getAvailableTimetableSlots(Long classId, Long sectionId, Integer dayOfWeek) {
        log.info("[{}][{}] Fetching Available Slots: classId={}, sectionId={}, dayOfWeek={}",
                LogContext.getRequestId(), LogContext.getLogId(), classId, sectionId, dayOfWeek);

        List<TimeTable> timetables;
        if (sectionId != null) {
            TimeTable tt = timetableRepository.findByClassIdAndSectionIdAndIsDeletedFalse(classId, sectionId);
            timetables = tt != null ? List.of(tt) : Collections.emptyList();
        } else {
            timetables = timetableRepository.findByIsDeletedFalse().stream()
                    .filter(t -> t.getClassId().equals(classId))
                    .collect(Collectors.toList());
        }

        List<TimeSlotDTO> availableSlots = new ArrayList<>();
        
        for (TimeTable timetable : timetables) {
            if (timetable.getSlots() != null) {
                for (TimeSlotSubjectMapper slot : timetable.getSlots()) {
                    if (slot.getDay() != null && slot.getDay().equals(dayOfWeek) && Boolean.TRUE.equals(slot.getActive())) {
                        String subjectName = null;
                        if (slot.getSubjectId() != null) {
                            subjectName = subjectRepository.findById(slot.getSubjectId())
                                    .map(Subject::getSubjectName)
                                    .orElse("Unknown");
                        }

                        availableSlots.add(TimeSlotDTO.builder()
                                .id(slot.getId())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .subjectId(slot.getSubjectId())
                                .subjectName(subjectName)
                                .teacherId(slot.getTeacherId())
                                .teacherName(slot.getTeacherName())
                                .roomId(slot.getRoom())
                                .day(slot.getDay())
                                .build());
                    }
                }
            }
        }

        // Sort slot blocks by start time
        availableSlots.sort(Comparator.comparing(TimeSlotDTO::getStartTime));

        return StandardResponse.success(availableSlots, "Available timetable slot blocks fetched successfully.");
    }

    // =========================================================================
    //  DTO CONVERSION & MASTER LOOKUPS
    // =========================================================================

    private ProxyTemplateResponse toResponse(ProxyTemplate entity) {
        if (entity == null) return null;
        return ProxyTemplateResponse.builder()
                .id(entity.getId())
                .templateName(entity.getTemplateName())
                .substituteTeacherId(entity.getSubstituteTeacherId())
                .substituteTeacherName(entity.getSubstituteTeacherName())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ProxyAssignmentResponse toResponse(ProxyAssignment entity, TimeTable timetable, Map<Integer, String> cmMap) {
        if (entity == null) return null;

        String dayName = (entity.getDayOfWeek() != null && entity.getDayOfWeek() >= 1 && entity.getDayOfWeek() <= 7)
                ? DAY_NAMES[entity.getDayOfWeek()]
                : "Day & " + entity.getDayOfWeek();

        String className = null;
        String sectionName = null;
        if (entity.getClassId() != null) {
            className = cmMap.getOrDefault(entity.getClassId().intValue(), "Unknown");
        }
        if (entity.getSectionId() != null) {
            sectionName = cmMap.getOrDefault(entity.getSectionId().intValue(), null);
        }

        ProxyTemplate template = entity.getTemplate();

        return ProxyAssignmentResponse.builder()
                .id(entity.getId())
                .templateId(template != null ? template.getId() : null)
                .templateName(template != null ? template.getTemplateName() : null)
                .substituteTeacherId(template != null ? template.getSubstituteTeacherId() : null)
                .substituteTeacherName(template != null ? template.getSubstituteTeacherName() : null)
                .timetableId(entity.getTimetableId())
                .isClassTeacher(entity.getIsClassTeacher())
                .timetableName(timetable != null ? timetable.getTimetableName() : null)
                .slotId(entity.getSlotId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .dayOfWeek(entity.getDayOfWeek())
                .dayName(dayName)
                .proxyDate(entity.getProxyDate())
                .originalTeacherName(entity.getOriginalTeacherName())
                .subjectId(entity.getSubjectId())
                .subjectName(entity.getSubjectName())
                .classId(entity.getClassId())
                .className(className)
                .sectionId(entity.getSectionId())
                .sectionName(sectionName)
                .remarks(entity.getRemarks())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Map<Integer, String> buildCommonMasterMap() {
        return commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(
                        com.academic.entity.CommonMaster::getId,
                        cm -> cm.getData() != null ? cm.getData() : cm.getCommonMasterKey()
                ));
    }
}
