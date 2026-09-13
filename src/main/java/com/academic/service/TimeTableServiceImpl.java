package com.academic.service;

import com.academic.entity.CommonMaster;
import com.academic.entity.Staff;
import com.academic.entity.Subject;
import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.TimeTableMapper;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.StaffRepository;
import com.academic.repository.SubjectRepository;
import com.academic.repository.TimeSlotSubjectMapperRepository;
import com.academic.repository.TimeTableRepository;
import com.academic.request.TimeSlotDTO;
import com.academic.request.TimeTableRequest;
import com.academic.response.LogContext;
import com.academic.response.StandardResponse;
import com.academic.response.TeacherTimetableResponse;
import com.academic.response.TeacherTimetableResponse.TeacherSlotDTO;
import com.academic.response.TimeTableResponse;
import com.academic.utility.Template;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableServiceImpl implements TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final TimeSlotSubjectMapperRepository mapperRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final SubjectRepository subjectRepository;
    private final StaffRepository staffRepository;

    @Autowired
    private TimeTableMapper timeTableMapper;

    // ---------------------------------------------------------------------------------------------------
    @Override
    @Transactional
    public StandardResponse<TimeTableResponse> create(TimeTableRequest request) {

        log.info("[{}][{}] Creating timetable: {}",
                LogContext.getRequestId(),
                LogContext.getLogId(),
                request.getTimetableName());

        try {

            // Check duplicate timetable name for the same class & section
            boolean exists = timeTableRepository
                    .existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalse(
                            request.getTimetableName(),
                            request.getClassId(),
                            request.getSectionId());

            if (exists) {
                return StandardResponse.error(
                        "Timetable with the same name already exists.",
                        "DUPLICATE_TIMETABLE",
                        "timetableName",
                        "Timetable '" + request.getTimetableName()
                                + "' already exists for the selected class and section."
                );
            }

            // Check if timetable already exists for the class & section
            boolean existsByClass = timeTableRepository
                    .existsByClassIdAndSectionIdAndIsDeletedFalse(
                            request.getClassId(),
                            request.getSectionId());

            if (existsByClass) {
                return StandardResponse.error(
                        "Timetable already exists for the selected class and section.",
                        "TIMETABLE_ALREADY_CREATED",
                        "classId",
                        "Only one timetable is allowed for a class and section."
                );
            }

            // ---------------------------------------------------------------
            // Teacher conflict validation:
            // A teacher must NOT be assigned to two timetable slots that share
            // the same day AND time window (startTime + endTime), even across
            // different timetables / classes / sections.
            // ---------------------------------------------------------------
            if (request.getSlots() != null) {

                // Build a lazy map of commonMaster for the error message
                Map<Integer, String> cmMap = commonMasterRepository.findAll()
                        .stream()
                        .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                        .collect(Collectors.toMap(
                                CommonMaster::getId,
                                cm -> cm.getData() != null ? cm.getData() : cm.getCommonMasterKey()
                        ));

                String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                for (TimeSlotDTO slot : request.getSlots()) {

                    // Skip incomplete slot definitions (no time window) — nothing to validate
                    if (slot.getStartTime() == null || slot.getEndTime() == null || slot.getDay() == null) {
                        continue;
                    }

                    // Teacher is optional - skip conflict checks if not assigned
                    if (slot.getTeacherId() == null) {
                        continue;
                    }

                    List<TimeSlotSubjectMapper> conflicts = mapperRepository.findConflictingSlots(
                            slot.getTeacherId(),
                            slot.getDay(),
                            slot.getStartTime(),
                            slot.getEndTime()
                    );

                    if (!conflicts.isEmpty()) {

                        TimeSlotSubjectMapper conflict = conflicts.get(0);

                        // Resolve subject name for the conflicting slot
                        String conflictSubject = subjectRepository
                                .findById(conflict.getSubjectId())
                                .map(s -> s.getSubjectName())
                                .orElse("Unknown Subject");

                        // Resolve class & section names for the conflicting timetable
                        TimeTable conflictTT = conflict.getTimeTable();
                        String conflictClass = conflictTT.getClassId() != null
                                ? cmMap.getOrDefault(conflictTT.getClassId().intValue(), "Unknown Class")
                                : "Unknown Class";
                        String conflictSection = conflictTT.getSectionId() != null
                                ? cmMap.getOrDefault(conflictTT.getSectionId().intValue(), "")
                                : "";

                        String sectionPart = conflictSection.isEmpty()
                                ? ""
                                : " section " + conflictSection;

                        String teacherLabel = slot.getTeacherName() != null
                                ? slot.getTeacherName()
                                : "Teacher (id=" + slot.getTeacherId() + ")";

                        String errorMsg = String.format(
                                "%s is already assigned in the time slot of %s - %s for subject %s in class %s%s.",
                                teacherLabel,
                                conflict.getStartTime(),
                                conflict.getEndTime(),
                                conflictSubject,
                                conflictClass,
                                sectionPart
                        );

                        return StandardResponse.error(
                                errorMsg,
                                "TEACHER_SLOT_CONFLICT",
                                "slots",
                                errorMsg
                        );
                    }
                }
            }

            // Create entity
            TimeTable entity = TimeTable.builder()
                    .timetableName(request.getTimetableName())
                    .classId(request.getClassId())
                    .sectionId(request.getSectionId())
                    .daysCoveredId(request.getDaysCoveredId())
                    .isDeleted(false)
                    .build();

            // Map slots
            entity.setSlots(
                    timeTableMapper.toEntityList(
                            request.getSlots(),
                            entity
                    )
            );

            // Save
            TimeTable saved = timeTableRepository.save(entity);

            // Common master map
            Map<Integer, String> commonMasterMap = commonMasterRepository.findAll()
                    .stream()
                    .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                    .collect(Collectors.toMap(
                            CommonMaster::getId,
                            CommonMaster::getCommonMasterKey
                    ));

            TimeTableResponse response =
                    timeTableMapper.toResponse(saved, commonMasterMap);

            return StandardResponse.success(
                    response,
                    "Timetable created successfully."
            );

        } catch (Exception ex) {

            log.error("[{}][{}] Error while creating timetable",
                    LogContext.getRequestId(),
                    LogContext.getLogId(),
                    ex);

            return StandardResponse.error(
                    "Failed to create timetable.",
                    "INTERNAL_SERVER_ERROR",
                    ex.getMessage()
            );
        }
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
                page != null && page > 0 ? page  : 0,
                size != null && size > 0 ? size : 10,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<TimeTable> timetablePage = timeTableRepository.findAllByFilters(classId, section, search, pageable);

        // Prepare common master map
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(CommonMaster::getId, CommonMaster::getData));

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

    @Override
    @Transactional(readOnly = true)
    public byte[] generateTimetablePdf(Long id) {
        log.info("[{}][{}] Generating PDF for timetable id {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);

        TimeTable entity = timeTableRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeTable not found with id: " + id));

        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .filter(cm -> Boolean.TRUE.equals(cm.getStatus()))
                .collect(Collectors.toMap(
                        CommonMaster::getId,
                        cm -> cm.getData() != null ? cm.getData() : cm.getCommonMasterKey()
                ));

        String className = entity.getClassId() != null ? commonMasterMap.get(entity.getClassId().intValue()) : "N/A";
        String sectionName = entity.getSectionId() != null ? commonMasterMap.get(entity.getSectionId().intValue()) : "N/A";
        String daysCovered = entity.getDaysCoveredId() != null ? commonMasterMap.get(entity.getDaysCoveredId().intValue()) : "N/A";

        List<TimeSlotSubjectMapper> slots = entity.getSlots();

        int maxDay = 6;
        for (TimeSlotSubjectMapper slot : slots) {
            if (slot.getDay() != null && slot.getDay() > maxDay) {
                maxDay = slot.getDay();
            }
        }

        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        StringBuilder dayHeaders = new StringBuilder();
        for (int d = 1; d <= maxDay; d++) {
            dayHeaders.append("<th>").append(dayNames[d]).append("</th>");
        }

        Map<TimeSlotRow, Map<Integer, TimeSlotSubjectMapper>> grid = new TreeMap<>();
        for (TimeSlotSubjectMapper slot : slots) {
            if (slot.getStartTime() == null || slot.getEndTime() == null || slot.getDay() == null) {
                continue;
            }
            TimeSlotRow key = new TimeSlotRow(slot.getStartTime(), slot.getEndTime());
            TimeSlotRow existingKey = grid.keySet().stream()
                    .filter(k -> k.equals(key))
                    .findFirst()
                    .orElse(key);
            grid.computeIfAbsent(existingKey, k -> new HashMap<>()).put(slot.getDay(), slot);
        }

        StringBuilder gridRows = new StringBuilder();
        for (TimeSlotRow r : grid.keySet()) {
            gridRows.append("<tr>");

            // Format time slot display nicely (e.g. 09:00 AM - 10:00 AM)
            String formattedTime = formatTimeSlotStr(r.getStartTime(), r.getEndTime());
            gridRows.append("<td class=\"time-cell\">").append(formattedTime).append("</td>");

            Map<Integer, TimeSlotSubjectMapper> dayMap = grid.get(r);
            for (int d = 1; d <= maxDay; d++) {
                TimeSlotSubjectMapper slot = dayMap.get(d);
                gridRows.append("<td>");
                if (slot != null) {
                    String subjectName = subjectRepository.findById(slot.getSubjectId())
                            .map(Subject::getSubjectName)
                            .orElse("Unknown");
                    String teacher = slot.getTeacherName() != null ? slot.getTeacherName() : "";
                    String room = slot.getRoom() != null ? slot.getRoom() : "";

                    gridRows.append("<div class=\"subject-name\">").append(escapeHtml(subjectName)).append("</div>");
                    if (!teacher.isEmpty()) {
                        gridRows.append("<div class=\"teacher-name\">").append(escapeHtml(teacher)).append("</div>");
                    }
                    if (!room.isEmpty()) {
                        gridRows.append("<div class=\"room-badge\">").append(escapeHtml(room)).append("</div>");
                    }
                } else {
                    gridRows.append("<span class=\"empty-cell\">-</span>");
                }
                gridRows.append("</td>");
            }

            gridRows.append("</tr>");
        }

        String sessionText;
        try {
            sessionText = StudentMobileServiceImpl.getCurrentSession();
        } catch (Exception e) {
            sessionText = "2026-27";
        }

        String html = Template.TIMETABLE_PDF_HTML;
        html = html.replace("${SESSION}", sessionText)
                .replace("${TIMETABLE_NAME}", escapeHtml(entity.getTimetableName()))
                .replace("${CLASS_SECTION}", escapeHtml(className + " - " + sectionName))
                .replace("${DAYS_COVERED}", escapeHtml(daysCovered))
                .replace("${PRINT_DATE}", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .replace("${DAY_HEADERS}", dayHeaders.toString())
                .replace("${GRID_ROWS}", gridRows.toString());

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for timetable: {}", id, e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private String formatTimeSlotStr(String start, String end) {
        return formatSingleTime(start) + " - " + formatSingleTime(end);
    }

    private String formatSingleTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return "";
        timeStr = timeStr.trim().toUpperCase().replaceAll("\\s+", " ");
        String[] formats = {"hh:mm a", "h:mm a", "HH:mm", "H:mm", "HH:mm:ss", "hh:mm:ssa"};
        LocalTime lt = null;
        for (String format : formats) {
            try {
                lt = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
                break;
            } catch (Exception ignored) {
            }
        }
        if (lt == null) {
            try {
                lt = LocalTime.parse(timeStr);
            } catch (Exception ignored) {
            }
        }
        if (lt != null) {
            return lt.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
        }
        return timeStr;
    }

    // ===========================================================================================
    // TEACHER WEEKLY TIMETABLE (JSON & PDF)
    // ===========================================================================================

    private String formatTeacherName(Staff staff) {
        if (staff == null) return "Unknown";
        String first = staff.getFirstName() != null ? staff.getFirstName().trim() : "";
        String last = staff.getLastName() != null ? staff.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Teacher" : full;
    }

    private String resolveSubjectName(Long subjectId) {
        if (subjectId == null) return "Unknown";
        Optional<Subject> subOpt = subjectRepository.findById(subjectId);
        if (subOpt.isPresent() && subOpt.get().getSubjectName() != null && !subOpt.get().getSubjectName().isBlank()) {
            return subOpt.get().getSubjectName();
        }
        return commonMasterRepository.findById(subjectId.intValue())
                .map(cm -> cm.getData() != null && !cm.getData().isBlank() ? cm.getData() : cm.getCommonMasterKey())
                .orElse("Unknown");
    }

    private String resolveClassName(Long classId, Map<Integer, String> cmMap) {
        if (classId == null) return "N/A";
        return cmMap.getOrDefault(classId.intValue(), "Class " + classId);
    }

    private String resolveSectionName(Long sectionId, Map<Integer, String> cmMap) {
        if (sectionId == null) return "";
        return cmMap.getOrDefault(sectionId.intValue(), "");
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherTimetableResponse getTeacherTimetable(Long staffId) {
        log.info("[{}][{}] Fetching Teacher Timetable JSON for staffId={}",
                LogContext.getRequestId(), LogContext.getLogId(), staffId);

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        String teacherName = formatTeacherName(staff);
        String staffCode = staff.getStaffCode() != null ? staff.getStaffCode() : "N/A";
        String department = (staff.getDepartment() != null && staff.getDepartment().getName() != null)
                ? staff.getDepartment().getName() : "N/A";

        // Query only active slots belonging to non-deleted timetables for this teacher
        List<TimeSlotSubjectMapper> rawSlots = mapperRepository.findActiveSlotsByTeacherId(staffId);
        List<TimeSlotSubjectMapper> activeSlots = new ArrayList<>();
        for (TimeSlotSubjectMapper s : rawSlots) {
            if (s.getTimeTable() != null && !Boolean.TRUE.equals(s.getTimeTable().getIsDeleted())) {
                activeSlots.add(s);
            }
        }

        // Fetch all distinct period rows that exist across active timetables in the school
        List<TimeSlotRow> schoolRows = getDistinctSchoolTimeSlotRows(activeSlots);

        // Determine working days (default Mon-Sat = 6)
        int maxDay = determineMaxDay(schoolRows, activeSlots);

        // Build CommonMaster lookup map
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .collect(Collectors.toMap(
                        CommonMaster::getId,
                        cm -> cm.getData() != null && !cm.getData().isBlank() ? cm.getData() : cm.getCommonMasterKey(),
                        (existing, replacement) -> existing
                ));

        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Map teacher slots by Day -> Row
        Map<Integer, Map<TimeSlotRow, TimeSlotSubjectMapper>> teacherSlotsByDay = new HashMap<>();
        for (TimeSlotSubjectMapper slot : activeSlots) {
            if (slot.getDay() == null || slot.getStartTime() == null || slot.getEndTime() == null) continue;
            TimeSlotRow slotRow = new TimeSlotRow(slot.getStartTime(), slot.getEndTime());
            TimeSlotRow canonicalRow = schoolRows.stream().filter(r -> r.equals(slotRow)).findFirst().orElse(slotRow);
            teacherSlotsByDay.computeIfAbsent(slot.getDay(), d -> new HashMap<>()).put(canonicalRow, slot);
        }

        List<TeacherSlotDTO> assignedSlots = new ArrayList<>();
        List<TeacherSlotDTO> freeSlots = new ArrayList<>();
        Map<String, List<TeacherSlotDTO>> scheduleByDay = new LinkedHashMap<>();
        Map<String, List<TeacherSlotDTO>> freeScheduleByDay = new LinkedHashMap<>();
        Map<String, List<TeacherSlotDTO>> fullWeeklyGrid = new LinkedHashMap<>();

        for (int i = 1; i <= maxDay; i++) {
            scheduleByDay.put(dayNames[i], new ArrayList<>());
            freeScheduleByDay.put(dayNames[i], new ArrayList<>());
            fullWeeklyGrid.put(dayNames[i], new ArrayList<>());
        }

        for (int d = 1; d <= maxDay; d++) {
            String dayName = dayNames[d];
            for (TimeSlotRow r : schoolRows) {
                TimeSlotSubjectMapper slot = teacherSlotsByDay.getOrDefault(d, Collections.emptyMap()).get(r);
                if (slot != null) {
                    TimeTable tt = slot.getTimeTable();
                    String subjectName = resolveSubjectName(slot.getSubjectId());
                    String className = tt != null ? resolveClassName(tt.getClassId(), commonMasterMap) : "N/A";
                    String sectionName = tt != null ? resolveSectionName(tt.getSectionId(), commonMasterMap) : "";
                    String classSection = sectionName.isBlank() ? className : className + " - " + sectionName;

                    TeacherSlotDTO dto = TeacherSlotDTO.builder()
                            .slotId(slot.getId())
                            .day(d)
                            .dayName(dayName)
                            .startTime(formatSingleTime(r.getStartTime()))
                            .endTime(formatSingleTime(r.getEndTime()))
                            .subjectId(slot.getSubjectId())
                            .subjectName(subjectName)
                            .classId(tt != null ? tt.getClassId() : null)
                            .className(className)
                            .sectionId(tt != null ? tt.getSectionId() : null)
                            .sectionName(sectionName)
                            .classSection(classSection)
                            .room(slot.getRoom() != null ? slot.getRoom() : "")
                            .timetableId(tt != null ? tt.getId() : null)
                            .timetableName(tt != null ? tt.getTimetableName() : null)
                            .isFree(false)
                            .status("ASSIGNED")
                            .build();

                    assignedSlots.add(dto);
                    scheduleByDay.get(dayName).add(dto);
                    fullWeeklyGrid.get(dayName).add(dto);
                } else {
                    TeacherSlotDTO freeDto = TeacherSlotDTO.builder()
                            .slotId(null)
                            .day(d)
                            .dayName(dayName)
                            .startTime(formatSingleTime(r.getStartTime()))
                            .endTime(formatSingleTime(r.getEndTime()))
                            .subjectId(null)
                            .subjectName("FREE")
                            .classId(null)
                            .className(null)
                            .sectionId(null)
                            .sectionName(null)
                            .classSection("Free Period")
                            .room("")
                            .timetableId(null)
                            .timetableName(null)
                            .isFree(true)
                            .status("FREE")
                            .build();

                    freeSlots.add(freeDto);
                    freeScheduleByDay.get(dayName).add(freeDto);
                    fullWeeklyGrid.get(dayName).add(freeDto);
                }
            }
        }

        int totalPeriods = assignedSlots.size();
        int workingDays = maxDay;
        int totalSchoolPeriods = schoolRows.size() * maxDay;
        int freePeriods = Math.max(0, totalSchoolPeriods - totalPeriods);

        return TeacherTimetableResponse.builder()
                .teacherId(staffId)
                .teacherName(teacherName)
                .staffCode(staffCode)
                .department(department)
                .totalPeriods(totalPeriods)
                .workingDays(workingDays)
                .totalSchoolPeriods(totalSchoolPeriods)
                .freePeriods(freePeriods)
                .slots(assignedSlots)
                .freeSlots(freeSlots)
                .scheduleByDay(scheduleByDay)
                .freeScheduleByDay(freeScheduleByDay)
                .fullWeeklyGrid(fullWeeklyGrid)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateTeacherTimetablePdf(Long staffId) {
        log.info("[{}][{}] Generating Teacher Timetable PDF for staffId={}",
                LogContext.getRequestId(), LogContext.getLogId(), staffId);

        // ── 1. Fetch Staff details ──────────────────────────────────────────────
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        String teacherName = formatTeacherName(staff);
        String staffCode   = staff.getStaffCode() != null ? staff.getStaffCode() : "N/A";
        String department  = (staff.getDepartment() != null && staff.getDepartment().getName() != null)
                ? staff.getDepartment().getName() : "N/A";

        // ── 2. Fetch only active slots from non-deleted timetables ──────────────
        List<TimeSlotSubjectMapper> rawSlots = mapperRepository.findActiveSlotsByTeacherId(staffId);
        List<TimeSlotSubjectMapper> slots = new ArrayList<>();
        for (TimeSlotSubjectMapper s : rawSlots) {
            if (s.getTimeTable() != null && !Boolean.TRUE.equals(s.getTimeTable().getIsDeleted())) {
                slots.add(s);
            }
        }

        // ── 3. Build CommonMaster lookup map ────────────────────────────────────
        Map<Integer, String> commonMasterMap = commonMasterRepository.findAll().stream()
                .collect(Collectors.toMap(
                        CommonMaster::getId,
                        cm -> cm.getData() != null && !cm.getData().isBlank() ? cm.getData() : cm.getCommonMasterKey(),
                        (existing, replacement) -> existing
                ));

        // ── 4. Determine school-wide period rows & max day ──────────────────────
        List<TimeSlotRow> schoolRows = getDistinctSchoolTimeSlotRows(slots);
        int maxDay = determineMaxDay(schoolRows, slots);

        // ── 5. Build day-header HTML ─────────────────────────────────────────────
        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        StringBuilder dayHeaders = new StringBuilder();
        for (int d = 1; d <= maxDay; d++) {
            dayHeaders.append("<th>").append(dayNames[d]).append("</th>");
        }

        // ── 6. Map teacher slots by Day -> Row ──────────────────────────────────
        Map<Integer, Map<TimeSlotRow, TimeSlotSubjectMapper>> teacherSlotsByDay = new HashMap<>();
        for (TimeSlotSubjectMapper slot : slots) {
            if (slot.getDay() == null || slot.getStartTime() == null || slot.getEndTime() == null) continue;
            TimeSlotRow slotRow = new TimeSlotRow(slot.getStartTime(), slot.getEndTime());
            TimeSlotRow canonicalRow = schoolRows.stream().filter(r -> r.equals(slotRow)).findFirst().orElse(slotRow);
            teacherSlotsByDay.computeIfAbsent(slot.getDay(), d -> new HashMap<>()).put(canonicalRow, slot);
        }

        // ── 7. Build grid HTML rows ──────────────────────────────────────────────
        int totalPeriods = 0;

        StringBuilder gridRows = new StringBuilder();
        for (TimeSlotRow r : schoolRows) {
            gridRows.append("<tr>");

            // Time column
            String formattedTime = formatTimeSlotStr(r.getStartTime(), r.getEndTime());
            gridRows.append("<td class=\"time-cell\">").append(formattedTime).append("</td>");

            for (int d = 1; d <= maxDay; d++) {
                TimeSlotSubjectMapper slot = teacherSlotsByDay.getOrDefault(d, Collections.emptyMap()).get(r);
                gridRows.append("<td>");
                if (slot != null) {
                    totalPeriods++;

                    // Subject name
                    String subjectName = resolveSubjectName(slot.getSubjectId());

                    // Class & Section label
                    String classLabel = "N/A";
                    if (slot.getTimeTable() != null) {
                        TimeTable tt = slot.getTimeTable();
                        String className = resolveClassName(tt.getClassId(), commonMasterMap);
                        String sectionName = resolveSectionName(tt.getSectionId(), commonMasterMap);
                        classLabel = sectionName.isBlank() ? className : className + " - " + sectionName;
                    }

                    String room = slot.getRoom() != null && !slot.getRoom().isBlank() ? slot.getRoom() : "";

                    gridRows.append("<div class=\"subject-name\">").append(escapeHtml(subjectName)).append("</div>");
                    gridRows.append("<div class=\"class-badge\">").append(escapeHtml(classLabel)).append("</div>");
                    if (!room.isEmpty()) {
                        gridRows.append("<br/><div class=\"room-badge\">").append(escapeHtml(room)).append("</div>");
                    }
                } else {
                    gridRows.append("<div class=\"free-cell\">FREE</div>");
                }
                gridRows.append("</td>");
            }
            gridRows.append("</tr>");
        }

        // ── 8. Compute summary numbers ───────────────────────────────────────────
        int workingDays = maxDay;
        int totalSlotCells = schoolRows.size() * maxDay;
        int freePeriods    = Math.max(0, totalSlotCells - totalPeriods);

        // ── 9. Session label ─────────────────────────────────────────────────────
        String sessionText;
        try { sessionText = StudentMobileServiceImpl.getCurrentSession(); }
        catch (Exception e) { sessionText = "2026-27"; }

        // ── 10. Fill template placeholders ──────────────────────────────────────
        String html = Template.TEACHER_TIMETABLE_PDF_HTML
                .replace("${SESSION}",       escapeHtml(sessionText))
                .replace("${TEACHER_NAME}",  escapeHtml(teacherName))
                .replace("${TEACHER_CODE}",  escapeHtml(staffCode))
                .replace("${DEPARTMENT}",    escapeHtml(department))
                .replace("${PRINT_DATE}",    LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .replace("${DAY_HEADERS}",   dayHeaders.toString())
                .replace("${GRID_ROWS}",     gridRows.toString())
                .replace("${TOTAL_PERIODS}", String.valueOf(totalPeriods))
                .replace("${WORKING_DAYS}",  String.valueOf(workingDays))
                .replace("${FREE_PERIODS}",  String.valueOf(freePeriods));

        // ── 11. Render PDF ───────────────────────────────────────────────────────
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            log.error("[{}][{}] Failed to generate teacher timetable PDF for staffId={}",
                    LogContext.getRequestId(), LogContext.getLogId(), staffId, e);
            throw new RuntimeException("Teacher timetable PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to retrieve all distinct time slot periods across the school
     * (active timetables + teacher's slots), chronologically sorted.
     */
    private List<TimeSlotRow> getDistinctSchoolTimeSlotRows(List<TimeSlotSubjectMapper> teacherSlots) {
        Set<TimeSlotRow> rowSet = new TreeSet<>();

        try {
            List<TimeSlotSubjectMapper> allSchoolSlots = mapperRepository.findAllActiveSchoolSlots();
            for (TimeSlotSubjectMapper s : allSchoolSlots) {
                if (s.getStartTime() != null && !s.getStartTime().isBlank() &&
                        s.getEndTime() != null && !s.getEndTime().isBlank()) {
                    rowSet.add(new TimeSlotRow(s.getStartTime(), s.getEndTime()));
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch all school slots from mapperRepository: {}", e.getMessage());
        }

        if (teacherSlots != null) {
            for (TimeSlotSubjectMapper s : teacherSlots) {
                if (s.getStartTime() != null && !s.getStartTime().isBlank() &&
                        s.getEndTime() != null && !s.getEndTime().isBlank()) {
                    rowSet.add(new TimeSlotRow(s.getStartTime(), s.getEndTime()));
                }
            }
        }

        return new ArrayList<>(rowSet);
    }

    /**
     * Helper to determine max working days (default Mon-Sat = 6, or 7 if Sunday has slots).
     */
    private int determineMaxDay(List<TimeSlotRow> schoolRows, List<TimeSlotSubjectMapper> teacherSlots) {
        int maxDay = 6;
        try {
            List<TimeSlotSubjectMapper> allSchoolSlots = mapperRepository.findAllActiveSchoolSlots();
            for (TimeSlotSubjectMapper s : allSchoolSlots) {
                if (s.getDay() != null && s.getDay() > maxDay) {
                    maxDay = s.getDay();
                }
            }
        } catch (Exception ignored) {
        }
        if (teacherSlots != null) {
            for (TimeSlotSubjectMapper s : teacherSlots) {
                if (s.getDay() != null && s.getDay() > maxDay) {
                    maxDay = s.getDay();
                }
            }
        }
        return maxDay;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Shared helper class for time-slot row grouping, sorting and equality.
     */
    private static class TimeSlotRow implements Comparable<TimeSlotRow> {
        private final String startTime;
        private final String endTime;

        public TimeSlotRow(String startTime, String endTime) {
            this.startTime = startTime != null ? startTime.trim() : "";
            this.endTime = endTime != null ? endTime.trim() : "";
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeSlotRow that = (TimeSlotRow) o;
            return Objects.equals(parseTime(this.startTime), parseTime(that.startTime)) &&
                    Objects.equals(parseTime(this.endTime), parseTime(that.endTime));
        }

        @Override
        public int hashCode() {
            return Objects.hash(parseTime(this.startTime), parseTime(this.endTime));
        }

        @Override
        public int compareTo(TimeSlotRow o) {
            try {
                LocalTime thisStart = parseTime(this.startTime);
                LocalTime otherStart = parseTime(o.startTime);
                int cmp = thisStart.compareTo(otherStart);
                if (cmp != 0) return cmp;

                LocalTime thisEnd = parseTime(this.endTime);
                LocalTime otherEnd = parseTime(o.endTime);
                return thisEnd.compareTo(otherEnd);
            } catch (Exception e) {
                return this.startTime.compareTo(o.startTime);
            }
        }

        public static LocalTime parseTime(String timeStr) {
            if (timeStr == null || timeStr.trim().isEmpty()) return LocalTime.MIDNIGHT;
            timeStr = timeStr.trim().toUpperCase().replaceAll("\\s+", " ");
            String[] formats = {"hh:mm a", "h:mm a", "HH:mm", "H:mm", "HH:mm:ss", "hh:mm:ssa"};
            for (String format : formats) {
                try {
                    return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(format, Locale.ENGLISH));
                } catch (Exception ignored) {
                }
            }
            try {
                return LocalTime.parse(timeStr);
            } catch (Exception e) {
                return LocalTime.MIDNIGHT;
            }
        }
    }
}

