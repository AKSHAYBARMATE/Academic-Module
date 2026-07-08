package com.academic.service;

import com.academic.entity.CommonMaster;
import com.academic.entity.Subject;
import com.academic.entity.TimeSlotSubjectMapper;
import com.academic.entity.TimeTable;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.TimeTableMapper;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.SubjectRepository;
import com.academic.repository.TimeSlotSubjectMapperRepository;
import com.academic.repository.TimeTableRepository;
import com.academic.request.TimeSlotDTO;
import com.academic.request.TimeTableRequest;
import com.academic.response.LogContext;
import com.academic.response.StandardResponse;
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

                for (TimeSlotDTO slot : request.getSlots()) {

                    // Only validate slots where a teacher is explicitly assigned
                    if (slot.getTeacherId() == null
                            || slot.getDay() == null
                            || slot.getStartTime() == null
                            || slot.getEndTime() == null) {
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
                page != null && page > 0 ? page - 1 : 0,
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

        // Helper inner class for Row grouping and sorting
        class TimeSlotRow implements Comparable<TimeSlotRow> {
            private final String startTime;
            private final String endTime;

            public TimeSlotRow(String startTime, String endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
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

            private LocalTime parseTime(String timeStr) {
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

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

