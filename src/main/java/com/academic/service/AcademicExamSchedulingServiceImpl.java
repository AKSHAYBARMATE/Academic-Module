package com.academic.service;

import com.academic.config.LoginUser;
import com.academic.dto.exam.*;
import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.response.StandardResponse;
import com.academic.utility.IstClock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicExamSchedulingServiceImpl implements AcademicExamSchedulingService {

    private final AcademicExamRepository academicExamRepository;
    private final AcademicExamSubjectMatrixRepository subjectMatrixRepository;
    private final AcademicExamScheduleSlotRepository scheduleSlotRepository;
    private final AcademicExamStudentMarkRepository studentMarkRepository;
    private final AcademicExamMarksSubmissionRepository marksSubmissionRepository;
    private final StudentPromotionMapperRepository studentPromotionMapperRepository;
    private final StudentRepository studentRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final SessionRepository sessionRepository;
    private final StaffRepository staffRepository;
    private final LoginUser loginUser;

    private Long getCurrentUserId() {
        if (loginUser != null && loginUser.getUserId() != null) {
            return loginUser.getUserId();
        }
        return 1L;
    }

    private String getMasterName(Integer id) {
        if (id == null) return null;
        return commonMasterRepository.findByIdAndStatusTrue(id)
                .map(CommonMaster::getData)
                .orElse("Unknown");
    }

    private Session getActiveSession() {
        String sessionText = StudentMobileServiceImpl.getCurrentSession();
        Session session = sessionRepository.findBySession(sessionText);
        if (session == null) {
            session = sessionRepository.findByIsActiveTrue()
                    .orElseThrow(() -> new RuntimeException("No active academic session found"));
        }
        return session;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 3: Step 1 – Exam Setup
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<AcademicExamResponse> saveExamSetup(AcademicExamRequest request) {
        log.info("Saving exam setup: {}", request.getExamName());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            return StandardResponse.error(
                    "Exam end date cannot be before start date",
                    "INVALID_DATE_RANGE",
                    "startDate: " + request.getStartDate() + ", endDate: " + request.getEndDate()
            );
        }

        AcademicExam exam;
        if (request.getId() != null) {
            exam = academicExamRepository.findByIdAndIsDeletedFalse(request.getId())
                    .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + request.getId()));
            exam.setModifiedBy(getCurrentUserId());
            exam.setModifiedAt(IstClock.nowDateTime());
        } else {
            exam = new AcademicExam();
            exam.setCreatedBy(getCurrentUserId());
            exam.setCreatedOn(IstClock.nowDateTime());
            exam.setIsDeleted(false);
        }

        exam.setExamName(request.getExamName().trim());
        exam.setExamType(request.getExamType().trim());
        exam.setAcademicSession(request.getAcademicSession().trim());
        exam.setSessionId(request.getSessionId());
        exam.setClassIds(request.getClassIds() != null ? request.getClassIds() : new ArrayList<>());
        if (request.getAssessmentComponents() != null && !request.getAssessmentComponents().isEmpty()) {
            exam.setAssessmentComponents(new ArrayList<>(request.getAssessmentComponents()));
        } else if (exam.getAssessmentComponents() == null || exam.getAssessmentComponents().isEmpty()) {
            exam.setAssessmentComponents(new ArrayList<>(List.of("Theory", "Practical", "Internal")));
        }
        exam.setStartDate(request.getStartDate());
        exam.setEndDate(request.getEndDate());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            exam.setStatus(request.getStatus().trim());
        } else if (exam.getStatus() == null) {
            exam.setStatus("Draft");
        }
        exam.setDescription(request.getDescription());

        AcademicExam saved = academicExamRepository.save(exam);
        return StandardResponse.success(mapToExamResponse(saved), "Exam setup saved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<AcademicExamResponse> getExamSetup(Long id) {
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + id));
        return StandardResponse.success(mapToExamResponse(exam), "Exam setup fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<AcademicExamResponse>> listExams(
            String academicSession, String status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String cleanSession = (academicSession != null && !academicSession.isBlank()) ? academicSession.trim() : null;
        String cleanStatus = (status != null && !status.isBlank()) ? status.trim() : null;
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<AcademicExam> examPage = academicExamRepository.searchExams(cleanSession, cleanStatus, cleanSearch, pageable);
        Page<AcademicExamResponse> responsePage = examPage.map(this::mapToExamResponse);
        return StandardResponse.success(responsePage, "Exams list fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<AcademicExamResponse> updateExamStatus(Long id, String status) {
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + id));

        exam.setStatus(status != null ? status.trim() : "Draft");
        exam.setModifiedBy(getCurrentUserId());
        exam.setModifiedAt(IstClock.nowDateTime());
        AcademicExam saved = academicExamRepository.save(exam);
        return StandardResponse.success(mapToExamResponse(saved), "Exam status updated to " + status);
    }

    @Override
    @Transactional
    public StandardResponse<Map<String, Object>> deleteExam(Long id) {
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + id));

        exam.setIsDeleted(true);
        exam.setModifiedBy(getCurrentUserId());
        exam.setModifiedAt(IstClock.nowDateTime());
        academicExamRepository.save(exam);
        return StandardResponse.success(Map.of("id", id, "deleted", true), "Exam deleted successfully");
    }

    private AcademicExamResponse mapToExamResponse(AcademicExam exam) {
        List<String> names = new ArrayList<>();
        if (exam.getClassIds() != null) {
            for (Long cId : exam.getClassIds()) {
                String cName = getMasterName(cId.intValue());
                names.add(cName != null ? cName : String.valueOf(cId));
            }
        }
        String label = String.join(", ", names);

        return AcademicExamResponse.builder()
                .id(exam.getId())
                .examName(exam.getExamName())
                .examType(exam.getExamType())
                .academicSession(exam.getAcademicSession())
                .sessionId(exam.getSessionId())
                .classIds(exam.getClassIds())
                .classNames(names)
                .classesLabel(label)
                .assessmentComponents(exam.getAssessmentComponents() != null ? exam.getAssessmentComponents() : List.of("Theory", "Practical", "Internal"))
                .startDate(exam.getStartDate())
                .endDate(exam.getEndDate())
                .status(exam.getStatus())
                .description(exam.getDescription())
                .createdBy(exam.getCreatedBy())
                .createdOn(exam.getCreatedOn())
                .modifiedBy(exam.getModifiedBy())
                .modifiedAt(exam.getModifiedAt())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 3: Step 2 – Subject & Marks Matrix
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<SubjectMarksMatrixResponse> saveSubjectMatrix(SubjectMarksMatrixRequest request) {
        log.info("Saving subject & marks matrix for exam ID: {}", request.getExamId());
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + request.getExamId()));

        Long currentUserId = getCurrentUserId();
        List<String> configuredComponents = (exam.getAssessmentComponents() != null && !exam.getAssessmentComponents().isEmpty())
                ? exam.getAssessmentComponents()
                : List.of("Theory", "Practical", "Internal");

        List<SubjectMarksMatrixItemDto> savedItems = new ArrayList<>();

        for (SubjectMarksMatrixItemDto item : request.getMatrixItems()) {
            AcademicExamSubjectMatrix matrix;
            if (item.getId() != null) {
                matrix = subjectMatrixRepository.findByIdAndIsDeletedFalse(item.getId())
                        .orElse(new AcademicExamSubjectMatrix());
            } else {
                Optional<AcademicExamSubjectMatrix> existing = (request.getClassId() != null)
                        ? subjectMatrixRepository.findByExamIdAndClassIdAndSubjectIdAndIsDeletedFalse(
                                request.getExamId(), request.getClassId(), item.getSubjectId())
                        : subjectMatrixRepository.findByExamIdAndSubjectIdAndIsDeletedFalse(
                                request.getExamId(), item.getSubjectId());
                matrix = existing.orElse(new AcademicExamSubjectMatrix());
            }

            if (matrix.getId() == null) {
                matrix.setExamId(request.getExamId());
                matrix.setClassId(request.getClassId());
                matrix.setCreatedBy(currentUserId);
                matrix.setCreatedOn(IstClock.nowDateTime());
                matrix.setIsDeleted(false);
            } else {
                matrix.setModifiedBy(currentUserId);
                matrix.setModifiedAt(IstClock.nowDateTime());
            }

            matrix.setSubjectId(item.getSubjectId());
            String sName = item.getSubjectName();
            if (sName == null || sName.isBlank()) {
                sName = getMasterName(item.getSubjectId().intValue());
            }
            matrix.setSubjectName(sName);

            // Dynamically populate components configured in Exam Setup
            Map<String, Double> dynamicComponentMaxMarks = new LinkedHashMap<>();
            double componentSum = 0.0;
            for (String comp : configuredComponents) {
                Double val = null;
                if (item.getComponentMaxMarks() != null && item.getComponentMaxMarks().containsKey(comp)) {
                    val = item.getComponentMaxMarks().get(comp);
                } else if ("Theory".equalsIgnoreCase(comp)) {
                    val = item.getTheoryMaxMarks();
                } else if ("Practical".equalsIgnoreCase(comp)) {
                    val = item.getPracticalMaxMarks();
                } else if ("Internal".equalsIgnoreCase(comp)) {
                    val = item.getInternalMaxMarks();
                }
                double finalVal = val != null ? val : 0.0;
                dynamicComponentMaxMarks.put(comp, finalVal);
                componentSum += finalVal;
            }

            // Also include any extra components provided in item.getComponentMaxMarks()
            if (item.getComponentMaxMarks() != null) {
                for (Map.Entry<String, Double> entry : item.getComponentMaxMarks().entrySet()) {
                    if (!dynamicComponentMaxMarks.containsKey(entry.getKey())) {
                        double v = entry.getValue() != null ? entry.getValue() : 0.0;
                        dynamicComponentMaxMarks.put(entry.getKey(), v);
                        componentSum += v;
                    }
                }
            }

            matrix.setComponentMaxMarks(dynamicComponentMaxMarks);

            // Synchronize legacy columns if present in dynamic components
            matrix.setTheoryMaxMarks(dynamicComponentMaxMarks.get("Theory"));
            matrix.setPracticalMaxMarks(dynamicComponentMaxMarks.get("Practical"));
            matrix.setInternalMaxMarks(dynamicComponentMaxMarks.get("Internal"));

            double total = (item.getTotalMaxMarks() != null && item.getTotalMaxMarks() > 0)
                    ? item.getTotalMaxMarks()
                    : componentSum;
            matrix.setTotalMaxMarks(total);
            matrix.setPassingMarks(item.getPassingMarks() != null ? item.getPassingMarks() : Math.round(total * 0.33));
            matrix.setWeightagePercentage(item.getWeightagePercentage() != null ? item.getWeightagePercentage() : 100.0);

            AcademicExamSubjectMatrix saved = subjectMatrixRepository.save(matrix);
            savedItems.add(SubjectMarksMatrixItemDto.builder()
                    .id(saved.getId())
                    .classId(saved.getClassId())
                    .subjectId(saved.getSubjectId())
                    .subjectName(saved.getSubjectName())
                    .theoryMaxMarks(saved.getTheoryMaxMarks())
                    .practicalMaxMarks(saved.getPracticalMaxMarks())
                    .internalMaxMarks(saved.getInternalMaxMarks())
                    .totalMaxMarks(saved.getTotalMaxMarks())
                    .passingMarks(saved.getPassingMarks())
                    .weightagePercentage(saved.getWeightagePercentage())
                    .componentMaxMarks(saved.getComponentMaxMarks())
                    .build());
        }

        String clsName = request.getClassId() != null ? getMasterName(request.getClassId().intValue()) : null;
        SubjectMarksMatrixResponse response = SubjectMarksMatrixResponse.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .classId(request.getClassId())
                .className(clsName)
                .assessmentComponents(exam.getAssessmentComponents())
                .matrixItems(savedItems)
                .build();

        return StandardResponse.success(response, "Subject & marks matrix configured successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<SubjectMarksMatrixResponse> getSubjectMatrix(Long examId, Long classId) {
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + examId));

        List<AcademicExamSubjectMatrix> list = (classId != null)
                ? subjectMatrixRepository.findByExamIdAndClassIdAndIsDeletedFalseOrderBySubjectIdAsc(examId, classId)
                : subjectMatrixRepository.findByExamIdAndIsDeletedFalseOrderBySubjectIdAsc(examId);

        List<String> configuredComponents = (exam.getAssessmentComponents() != null && !exam.getAssessmentComponents().isEmpty())
                ? exam.getAssessmentComponents()
                : List.of("Theory", "Practical", "Internal");

        List<SubjectMarksMatrixItemDto> dtos = list.stream().map(m -> {
            Map<String, Double> cMarks = new LinkedHashMap<>();
            for (String comp : configuredComponents) {
                Double val = null;
                if (m.getComponentMaxMarks() != null && m.getComponentMaxMarks().containsKey(comp)) {
                    val = m.getComponentMaxMarks().get(comp);
                } else if ("Theory".equalsIgnoreCase(comp)) {
                    val = m.getTheoryMaxMarks();
                } else if ("Practical".equalsIgnoreCase(comp)) {
                    val = m.getPracticalMaxMarks();
                } else if ("Internal".equalsIgnoreCase(comp)) {
                    val = m.getInternalMaxMarks();
                }
                cMarks.put(comp, val != null ? val : 0.0);
            }
            if (m.getComponentMaxMarks() != null) {
                for (Map.Entry<String, Double> entry : m.getComponentMaxMarks().entrySet()) {
                    if (!cMarks.containsKey(entry.getKey())) {
                        cMarks.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            return SubjectMarksMatrixItemDto.builder()
                    .id(m.getId())
                    .classId(m.getClassId())
                    .subjectId(m.getSubjectId())
                    .subjectName(m.getSubjectName())
                    .theoryMaxMarks(m.getTheoryMaxMarks())
                    .practicalMaxMarks(m.getPracticalMaxMarks())
                    .internalMaxMarks(m.getInternalMaxMarks())
                    .totalMaxMarks(m.getTotalMaxMarks())
                    .passingMarks(m.getPassingMarks())
                    .weightagePercentage(m.getWeightagePercentage())
                    .componentMaxMarks(cMarks)
                    .build();
        }).collect(Collectors.toList());

        String clsName = classId != null ? getMasterName(classId.intValue()) : null;
        SubjectMarksMatrixResponse response = SubjectMarksMatrixResponse.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .classId(classId)
                .className(clsName)
                .assessmentComponents(exam.getAssessmentComponents())
                .matrixItems(dtos)
                .build();

        return StandardResponse.success(response, "Subject & marks matrix fetched successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 3: Step 3 – Exam Schedule Slots
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<ExamScheduleResponse> saveScheduleSlots(ExamScheduleRequest request) {
        log.info("Saving exam schedule slots for exam ID: {}", request.getExamId());
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + request.getExamId()));

        Long currentUserId = getCurrentUserId();

        // Build a set of classIds configured in this exam for fast lookup
        List<Long> allowedClassIds = exam.getClassIds();
        if (allowedClassIds == null || allowedClassIds.isEmpty()) {
            return StandardResponse.error(
                    "No classes are configured for this exam. Please update the exam setup first.",
                    "NO_CLASSES_CONFIGURED", null);
        }

        List<ExamScheduleSlotDto> resultDtos = new ArrayList<>();

        for (ExamScheduleSlotDto dto : request.getScheduleSlots()) {
            // Validation 0: Class must be one of the classes configured in the exam setup
            if (!allowedClassIds.contains(dto.getClassId())) {
                String className = getMasterName(dto.getClassId().intValue());
                String classLabel = className != null ? className : "ID " + dto.getClassId();
                return StandardResponse.error(
                        "Class '" + classLabel + "' is not part of this exam. Allowed classes: "
                                + allowedClassIds.stream()
                                        .map(id -> { String n = getMasterName(id.intValue()); return n != null ? n : String.valueOf(id); })
                                        .collect(Collectors.joining(", ")),
                        "CLASS_NOT_IN_EXAM", null);
            }

            // Validation 1: Schedule must be within exam start and end dates
            if (dto.getExamDate().isBefore(exam.getStartDate()) || dto.getExamDate().isAfter(exam.getEndDate())) {
                return StandardResponse.error(
                        "Schedule date " + dto.getExamDate() + " must be between exam start date ("
                                + exam.getStartDate() + ") and end date (" + exam.getEndDate() + ")",
                        "DATE_OUT_OF_RANGE", null);
            }

            // Validation 2: Prevent overlapping exams for the same class/section
            List<AcademicExamScheduleSlot> classConflicts = scheduleSlotRepository.findConflictingClassSlots(
                    dto.getClassId(), dto.getSectionId(), dto.getExamDate(), dto.getStartTime(), dto.getEndTime(), dto.getId());
            if (!classConflicts.isEmpty()) {
                AcademicExamScheduleSlot c = classConflicts.get(0);
                String subName = getMasterName(c.getSubjectId().intValue());
                return StandardResponse.error(
                        "Time slot overlap detected for Class/Section on " + dto.getExamDate()
                                + ". Conflicting subject: " + subName + " (" + c.getStartTime() + " - " + c.getEndTime() + ")",
                        "CLASS_SLOT_CONFLICT", null);
            }

            // Validation 3: Prevent room conflicts
            if (dto.getRoom() != null && !dto.getRoom().isBlank()) {
                List<AcademicExamScheduleSlot> roomConflicts = scheduleSlotRepository.findConflictingRoomSlots(
                        dto.getRoom().trim(), dto.getExamDate(), dto.getStartTime(), dto.getEndTime(), dto.getId());
                if (!roomConflicts.isEmpty()) {
                    return StandardResponse.error(
                            "Room " + dto.getRoom() + " is already occupied on " + dto.getExamDate()
                                    + " between " + dto.getStartTime() + " and " + dto.getEndTime(),
                            "ROOM_CONFLICT", null);
                }
            }

            AcademicExamScheduleSlot slot;
            if (dto.getId() != null) {
                slot = scheduleSlotRepository.findByIdAndIsDeletedFalse(dto.getId())
                        .orElse(new AcademicExamScheduleSlot());
                slot.setModifiedBy(currentUserId);
                slot.setModifiedAt(IstClock.nowDateTime());
            } else {
                slot = new AcademicExamScheduleSlot();
                slot.setCreatedBy(currentUserId);
                slot.setCreatedOn(IstClock.nowDateTime());
                slot.setIsDeleted(false);
            }

            slot.setExamId(exam.getId());
            slot.setExamDate(dto.getExamDate());
            slot.setClassId(dto.getClassId());
            slot.setSectionId(dto.getSectionId());
            slot.setSubjectId(dto.getSubjectId());
            slot.setStartTime(dto.getStartTime().trim());
            slot.setEndTime(dto.getEndTime().trim());
            slot.setDuration(dto.getDuration() != null ? dto.getDuration().trim() : "2 Hrs");
            slot.setRoom(dto.getRoom() != null ? dto.getRoom().trim() : null);
            slot.setInvigilatorId(dto.getInvigilatorId());

            String invName = dto.getInvigilatorName();
            if ((invName == null || invName.isBlank()) && dto.getInvigilatorId() != null) {
                Staff st = staffRepository.findByIsDeletedAndId(false, dto.getInvigilatorId());
                if (st != null) {
                    invName = st.getFirstName() + " " + st.getLastName();
                }
            }
            slot.setInvigilatorName(invName);
            slot.setMaxMarks(dto.getMaxMarks() != null ? dto.getMaxMarks() : 100.0);
            slot.setStatus(dto.getStatus() != null ? dto.getStatus() : "SCHEDULED");

            AcademicExamScheduleSlot saved = scheduleSlotRepository.save(slot);
            resultDtos.add(mapToSlotDto(saved));
        }

        ExamScheduleResponse response = ExamScheduleResponse.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .academicSession(exam.getAcademicSession())
                .examStatus(exam.getStatus())
                .slots(resultDtos)
                .build();

        return StandardResponse.success(response, "Exam schedule slots saved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ExamScheduleResponse> getScheduleSlots(Long examId, Long classId, Long sectionId) {
        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + examId));

        List<AcademicExamScheduleSlot> slots;
        if (classId != null && sectionId != null) {
            slots = scheduleSlotRepository.findByExamIdAndClassIdAndSectionIdAndIsDeletedFalseOrderByExamDateAscStartTimeAsc(examId, classId, sectionId);
        } else if (classId != null) {
            slots = scheduleSlotRepository.findByExamIdAndClassIdAndIsDeletedFalseOrderByExamDateAscStartTimeAsc(examId, classId);
        } else {
            slots = scheduleSlotRepository.findByExamIdAndIsDeletedFalseOrderByExamDateAscStartTimeAsc(examId);
        }

        List<ExamScheduleSlotDto> dtos = slots.stream().map(this::mapToSlotDto).collect(Collectors.toList());
        ExamScheduleResponse response = ExamScheduleResponse.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .academicSession(exam.getAcademicSession())
                .examStatus(exam.getStatus())
                .slots(dtos)
                .build();

        return StandardResponse.success(response, "Exam schedule slots fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Map<String, Object>> deleteScheduleSlot(Long slotId) {
        AcademicExamScheduleSlot slot = scheduleSlotRepository.findByIdAndIsDeletedFalse(slotId)
                .orElseThrow(() -> new RuntimeException("Schedule slot not found with ID: " + slotId));

        slot.setIsDeleted(true);
        slot.setModifiedBy(getCurrentUserId());
        slot.setModifiedAt(IstClock.nowDateTime());
        scheduleSlotRepository.save(slot);
        return StandardResponse.success(Map.of("id", slotId, "deleted", true), "Schedule slot deleted successfully");
    }

    private ExamScheduleSlotDto mapToSlotDto(AcademicExamScheduleSlot s) {
        String clsName = getMasterName(s.getClassId().intValue());
        String secName = s.getSectionId() != null ? getMasterName(s.getSectionId().intValue()) : null;
        String subName = getMasterName(s.getSubjectId().intValue());

        return ExamScheduleSlotDto.builder()
                .id(s.getId())
                .examDate(s.getExamDate())
                .classId(s.getClassId())
                .className(clsName != null ? clsName : String.valueOf(s.getClassId()))
                .sectionId(s.getSectionId())
                .sectionName(secName)
                .subjectId(s.getSubjectId())
                .subjectName(subName != null ? subName : String.valueOf(s.getSubjectId()))
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .duration(s.getDuration())
                .room(s.getRoom())
                .invigilatorId(s.getInvigilatorId())
                .invigilatorName(s.getInvigilatorName())
                .maxMarks(s.getMaxMarks())
                .status(s.getStatus())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 4: Marks Entry & Result Processing
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<MarksGridLoadResponse> loadMarksGrid(
            Long examId, Long classId, Long sectionId, Long subjectId, String assessmentComponent) {
        log.info("Loading marks grid for exam: {}, class: {}, section: {}, subject: {}",
                examId, classId, sectionId, subjectId);

        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + examId));

        // Validation rule from Screen 4: "Only scheduled subjects appear for marks entry"
        boolean isScheduled = scheduleSlotRepository
                .isSubjectScheduled(examId, classId, sectionId, subjectId);
        if (!isScheduled) {
            return StandardResponse.error(
                    "Selected subject is not scheduled for this class and section under " + exam.getExamName(),
                    "SUBJECT_NOT_SCHEDULED", null);
        }

        List<String> configuredComponents = (exam.getAssessmentComponents() != null && !exam.getAssessmentComponents().isEmpty())
                ? exam.getAssessmentComponents()
                : List.of("Theory", "Practical", "Internal");

        // Fetch subject & marks matrix
        Optional<AcademicExamSubjectMatrix> matrixOpt = subjectMatrixRepository
                .findByExamIdAndClassIdAndSubjectIdAndIsDeletedFalse(examId, classId, subjectId);
        if (matrixOpt.isEmpty()) {
            matrixOpt = subjectMatrixRepository.findByExamIdAndSubjectIdAndIsDeletedFalse(examId, subjectId);
        }
        AcademicExamSubjectMatrix matrix = matrixOpt.orElseGet(() -> {
            Map<String, Double> defaultCompMarks = new LinkedHashMap<>();
            for (String comp : configuredComponents) {
                defaultCompMarks.put(comp, 0.0);
            }
            return AcademicExamSubjectMatrix.builder()
                    .componentMaxMarks(defaultCompMarks)
                    .theoryMaxMarks(defaultCompMarks.get("Theory"))
                    .practicalMaxMarks(defaultCompMarks.get("Practical"))
                    .internalMaxMarks(defaultCompMarks.get("Internal"))
                    .totalMaxMarks(100.0)
                    .passingMarks(33.0)
                    .weightagePercentage(100.0)
                    .build();
        });

        // Check lock & submission status
        Optional<AcademicExamMarksSubmission> subOpt = marksSubmissionRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(examId, classId, sectionId, subjectId);
        String submissionStatus = subOpt.map(AcademicExamMarksSubmission::getStatus).orElse("DRAFT");
        boolean isLocked = "LOCKED".equalsIgnoreCase(submissionStatus);

        // Fetch students for class & section
        Session session = getActiveSession();
        List<StudentPromotionMapper> activePromotions = studentPromotionMapperRepository
                .findByToClassAndToSectionAndAcademicYear(classId.intValue(), sectionId.intValue(), session.getId());

        List<Integer> studentIds = activePromotions.stream().map(StudentPromotionMapper::getStudentId).collect(Collectors.toList());
        List<Student> students = studentIds.isEmpty() ? Collections.emptyList()
                : studentRepository.findByIdInAndStatusAndIsDeletedFalse(studentIds, 1);
        students.sort(Comparator.comparing(s -> (s.getFirstName() + " " + s.getLastName())));

        // Fetch existing marks
        List<AcademicExamStudentMark> existingMarks = studentMarkRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalseOrderByRollNoAsc(examId, classId, sectionId, subjectId);
        Map<Long, AcademicExamStudentMark> marksMap = existingMarks.stream()
                .collect(Collectors.toMap(AcademicExamStudentMark::getStudentId, m -> m, (a, b) -> a));

        List<StudentMarkItemDto> studentDtos = new ArrayList<>();
        int rollCounter = 1;
        for (Student stu : students) {
            Long stuId = Long.valueOf(stu.getId());
            AcademicExamStudentMark m = marksMap.get(stuId);

            String roll = (stu.getAdmissionNo() != null && !stu.getAdmissionNo().isBlank())
                    ? stu.getAdmissionNo() : String.valueOf(rollCounter++);
            String fullName = stu.getFirstName() + " " + stu.getLastName();

            Map<String, Double> cMarks = (m != null && m.getComponentMarks() != null && !m.getComponentMarks().isEmpty())
                    ? new HashMap<>(m.getComponentMarks())
                    : new HashMap<>();
            if (!cMarks.containsKey("Theory") && m != null && m.getTheoryMarks() != null) cMarks.put("Theory", m.getTheoryMarks());
            if (!cMarks.containsKey("Practical") && m != null && m.getPracticalMarks() != null) cMarks.put("Practical", m.getPracticalMarks());
            if (!cMarks.containsKey("Internal") && m != null && m.getInternalMarks() != null) cMarks.put("Internal", m.getInternalMarks());

            Map<String, String> cDisplays = new HashMap<>();
            String curAtt = m != null ? m.getAttendanceStatus() : "PRESENT";
            for (Map.Entry<String, Double> entry : cMarks.entrySet()) {
                cDisplays.put(entry.getKey(), formatDisplay(entry.getValue(), curAtt, entry.getKey()));
            }

            if (m != null) {
                studentDtos.add(StudentMarkItemDto.builder()
                        .id(m.getId())
                        .studentId(stuId)
                        .rollNo(m.getRollNo() != null ? m.getRollNo() : roll)
                        .studentName(fullName)
                        .theoryMarks(m.getTheoryMarks())
                        .practicalMarks(m.getPracticalMarks())
                        .internalMarks(m.getInternalMarks())
                        .componentMarks(cMarks)
                        .componentDisplays(cDisplays)
                        .theoryDisplay(formatDisplay(m.getTheoryMarks(), m.getAttendanceStatus(), "THEORY"))
                        .practicalDisplay(formatDisplay(m.getPracticalMarks(), m.getAttendanceStatus(), "PRACTICAL"))
                        .internalDisplay(formatDisplay(m.getInternalMarks(), m.getAttendanceStatus(), "INTERNAL"))
                        .totalMarks(m.getTotalMarks())
                        .percentage(m.getPercentage())
                        .attendanceStatus(m.getAttendanceStatus())
                        .grade(m.getGrade())
                        .result(m.getResult())
                        .remarks(m.getRemarks())
                        .build());
            } else {
                studentDtos.add(StudentMarkItemDto.builder()
                        .studentId(stuId)
                        .rollNo(roll)
                        .studentName(fullName)
                        .attendanceStatus("PRESENT")
                        .componentMarks(new HashMap<>())
                        .componentDisplays(new HashMap<>())
                        .grade("-")
                        .result("-")
                        .build());
            }
        }

        String className = getMasterName(classId.intValue());
        String sectionName = getMasterName(sectionId.intValue());
        String subjectName = getMasterName(subjectId.intValue());

        Map<String, Double> compMaxMarks = new LinkedHashMap<>();
        for (String comp : configuredComponents) {
            Double val = null;
            if (matrix.getComponentMaxMarks() != null && matrix.getComponentMaxMarks().containsKey(comp)) {
                val = matrix.getComponentMaxMarks().get(comp);
            } else if ("Theory".equalsIgnoreCase(comp)) {
                val = matrix.getTheoryMaxMarks();
            } else if ("Practical".equalsIgnoreCase(comp)) {
                val = matrix.getPracticalMaxMarks();
            } else if ("Internal".equalsIgnoreCase(comp)) {
                val = matrix.getInternalMaxMarks();
            }
            compMaxMarks.put(comp, val != null ? val : 0.0);
        }
        if (matrix.getComponentMaxMarks() != null) {
            for (Map.Entry<String, Double> e : matrix.getComponentMaxMarks().entrySet()) {
                if (!compMaxMarks.containsKey(e.getKey())) {
                    compMaxMarks.put(e.getKey(), e.getValue());
                }
            }
        }

        MarksGridLoadResponse response = MarksGridLoadResponse.builder()
                .examId(examId)
                .examName(exam.getExamName())
                .classId(classId)
                .className(className)
                .sectionId(sectionId)
                .sectionName(sectionName)
                .subjectId(subjectId)
                .subjectName(subjectName)
                .assessmentComponent(assessmentComponent != null ? assessmentComponent : "Theory")
                .assessmentComponents(exam.getAssessmentComponents() != null ? exam.getAssessmentComponents() : List.of("Theory", "Practical", "Internal"))
                .componentMaxMarks(compMaxMarks)
                .theoryMaxMarks(matrix.getTheoryMaxMarks())
                .practicalMaxMarks(matrix.getPracticalMaxMarks())
                .internalMaxMarks(matrix.getInternalMaxMarks())
                .totalMaxMarks(matrix.getTotalMaxMarks())
                .passingMarks(matrix.getPassingMarks())
                .weightagePercentage(matrix.getWeightagePercentage())
                .submissionStatus(submissionStatus)
                .isLocked(isLocked)
                .lockedByUser(subOpt.map(AcademicExamMarksSubmission::getLockedByUser).orElse(null))
                .lockedAt(subOpt.map(AcademicExamMarksSubmission::getLockedAt).orElse(null))
                .submittedByUser(subOpt.map(AcademicExamMarksSubmission::getSubmittedByUser).orElse(null))
                .submittedAt(subOpt.map(AcademicExamMarksSubmission::getSubmittedAt).orElse(null))
                .students(studentDtos)
                .build();

        return StandardResponse.success(response, "Marks grid loaded successfully");
    }

    private static String formatDisplay(Double val, String attStatus, String component) {
        if ("AB".equalsIgnoreCase(attStatus) || "ABSENT".equalsIgnoreCase(attStatus)) return "AB";
        if ("EX".equalsIgnoreCase(attStatus) || "EXEMPT".equalsIgnoreCase(attStatus)) return "EX";
        if ("NA".equalsIgnoreCase(attStatus) || "NOT_APPLICABLE".equalsIgnoreCase(attStatus)) return "NA";
        if (val == null) return null;
        return (val == Math.floor(val)) ? String.format("%.0f", val) : String.valueOf(val);
    }

    @Override
    @Transactional
    public StandardResponse<MarksGridLoadResponse> saveMarksDraft(MarksEntryBatchRequest request) {
        return processMarksBatch(request, "DRAFT");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<MarksValidationResponse> validateMarks(MarksEntryBatchRequest request) {
        log.info("Validating marks batch for exam: {}, subject: {}", request.getExamId(), request.getSubjectId());

        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(request.getExamId()).orElse(null);
        List<String> configuredComponents = (exam != null && exam.getAssessmentComponents() != null && !exam.getAssessmentComponents().isEmpty())
                ? exam.getAssessmentComponents()
                : List.of("Theory", "Practical", "Internal");

        Optional<AcademicExamSubjectMatrix> matrixOpt = subjectMatrixRepository
                .findByExamIdAndClassIdAndSubjectIdAndIsDeletedFalse(request.getExamId(), request.getClassId(), request.getSubjectId());
        if (matrixOpt.isEmpty()) {
            matrixOpt = subjectMatrixRepository.findByExamIdAndSubjectIdAndIsDeletedFalse(request.getExamId(), request.getSubjectId());
        }

        Map<String, Double> compMaxMarks = new LinkedHashMap<>();
        if (matrixOpt.isPresent() && matrixOpt.get().getComponentMaxMarks() != null && !matrixOpt.get().getComponentMaxMarks().isEmpty()) {
            compMaxMarks.putAll(matrixOpt.get().getComponentMaxMarks());
        }
        for (String comp : configuredComponents) {
            if (!compMaxMarks.containsKey(comp)) {
                Double v = null;
                if (matrixOpt.isPresent()) {
                    AcademicExamSubjectMatrix m = matrixOpt.get();
                    if ("Theory".equalsIgnoreCase(comp)) v = m.getTheoryMaxMarks();
                    else if ("Practical".equalsIgnoreCase(comp)) v = m.getPracticalMaxMarks();
                    else if ("Internal".equalsIgnoreCase(comp)) v = m.getInternalMaxMarks();
                }
                compMaxMarks.put(comp, v != null ? v : 100.0);
            }
        }
        double tMax = compMaxMarks.getOrDefault("Theory", 70.0);
        double pMax = compMaxMarks.getOrDefault("Practical", 10.0);
        double iMax = compMaxMarks.getOrDefault("Internal", 20.0);

        List<MarksValidationResponse.ValidationErrorItem> errors = new ArrayList<>();
        int validCount = 0;

        for (StudentMarkItemDto item : request.getMarksList()) {
            boolean itemValid = true;
            String att = item.getAttendanceStatus();
            if ("AB".equalsIgnoreCase(att) || "EX".equalsIgnoreCase(att) || "NA".equalsIgnoreCase(att)) {
                validCount++;
                continue;
            }

            // Validate dynamic components
            if (item.getComponentMarks() != null && !item.getComponentMarks().isEmpty()) {
                for (Map.Entry<String, Double> entry : item.getComponentMarks().entrySet()) {
                    String cName = entry.getKey();
                    Double cVal = entry.getValue();
                    Double maxVal = compMaxMarks.getOrDefault(cName, 100.0);
                    if (cVal != null && (cVal < 0 || cVal > maxVal)) {
                        itemValid = false;
                        errors.add(MarksValidationResponse.ValidationErrorItem.builder()
                                .studentId(item.getStudentId())
                                .rollNo(item.getRollNo())
                                .studentName(item.getStudentName())
                                .field(cName)
                                .message(cName + " marks (" + cVal + ") exceeds maximum allowed (" + maxVal + ")")
                                .build());
                    }
                }
            }

            if (item.getTheoryMarks() != null) {
                if (item.getTheoryMarks() < 0 || item.getTheoryMarks() > tMax) {
                    itemValid = false;
                    errors.add(MarksValidationResponse.ValidationErrorItem.builder()
                            .studentId(item.getStudentId())
                            .rollNo(item.getRollNo())
                            .studentName(item.getStudentName())
                            .field("theoryMarks")
                            .message("Theory marks (" + item.getTheoryMarks() + ") exceeds maximum allowed (" + tMax + ")")
                            .build());
                }
            }

            if (item.getPracticalMarks() != null) {
                if (item.getPracticalMarks() < 0 || item.getPracticalMarks() > pMax) {
                    itemValid = false;
                    errors.add(MarksValidationResponse.ValidationErrorItem.builder()
                            .studentId(item.getStudentId())
                            .rollNo(item.getRollNo())
                            .studentName(item.getStudentName())
                            .field("practicalMarks")
                            .message("Practical marks (" + item.getPracticalMarks() + ") exceeds maximum allowed (" + pMax + ")")
                            .build());
                }
            }

            if (item.getInternalMarks() != null) {
                if (item.getInternalMarks() < 0 || item.getInternalMarks() > iMax) {
                    itemValid = false;
                    errors.add(MarksValidationResponse.ValidationErrorItem.builder()
                            .studentId(item.getStudentId())
                            .rollNo(item.getRollNo())
                            .studentName(item.getStudentName())
                            .field("internalMarks")
                            .message("Internal marks (" + item.getInternalMarks() + ") exceeds maximum allowed (" + iMax + ")")
                            .build());
                }
            }

            if (itemValid) {
                validCount++;
            }
        }

        MarksValidationResponse response = MarksValidationResponse.builder()
                .isValid(errors.isEmpty())
                .totalStudents(request.getMarksList().size())
                .validStudents(validCount)
                .invalidStudents(errors.size())
                .errors(errors)
                .build();

        return StandardResponse.success(response, errors.isEmpty() ? "All marks entries are valid" : "Validation errors found in marks entry");
    }

    @Override
    @Transactional
    public StandardResponse<MarksGridLoadResponse> submitMarks(MarksEntryBatchRequest request) {
        // Pre-validate before submitting
        StandardResponse<MarksValidationResponse> valRes = validateMarks(request);
        if (!valRes.getData().isValid()) {
            return StandardResponse.error(
                    "Cannot submit marks due to validation errors. Please review and fix out-of-range marks.",
                    "VALIDATION_FAILED", null);
        }
        return processMarksBatch(request, "SUBMITTED");
    }

    private StandardResponse<MarksGridLoadResponse> processMarksBatch(MarksEntryBatchRequest request, String targetStatus) {
        Long examId = request.getExamId();
        Long classId = request.getClassId();
        Long sectionId = request.getSectionId();
        Long subjectId = request.getSubjectId();

        // Check if sheet is locked
        Optional<AcademicExamMarksSubmission> existingSub = marksSubmissionRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(examId, classId, sectionId, subjectId);
        if (existingSub.isPresent() && "LOCKED".equalsIgnoreCase(existingSub.get().getStatus())) {
            return StandardResponse.error(
                    "Marks sheet is locked and cannot be edited. Please request Principal / Admin to unlock.",
                    "SHEET_LOCKED", null);
        }

        AcademicExam exam = academicExamRepository.findByIdAndIsDeletedFalse(examId).orElse(null);
        List<String> configuredComponents = (exam != null && exam.getAssessmentComponents() != null && !exam.getAssessmentComponents().isEmpty())
                ? exam.getAssessmentComponents()
                : List.of("Theory", "Practical", "Internal");

        // Fetch matrix max marks & passing marks
        Optional<AcademicExamSubjectMatrix> matrixOpt = subjectMatrixRepository
                .findByExamIdAndClassIdAndSubjectIdAndIsDeletedFalse(examId, classId, subjectId);
        if (matrixOpt.isEmpty()) {
            matrixOpt = subjectMatrixRepository.findByExamIdAndSubjectIdAndIsDeletedFalse(examId, subjectId);
        }
        AcademicExamSubjectMatrix matrix = matrixOpt.orElseGet(() -> {
            Map<String, Double> defaultCompMarks = new LinkedHashMap<>();
            for (String comp : configuredComponents) {
                defaultCompMarks.put(comp, 0.0);
            }
            return AcademicExamSubjectMatrix.builder()
                    .componentMaxMarks(defaultCompMarks)
                    .theoryMaxMarks(defaultCompMarks.get("Theory"))
                    .practicalMaxMarks(defaultCompMarks.get("Practical"))
                    .internalMaxMarks(defaultCompMarks.get("Internal"))
                    .totalMaxMarks(100.0)
                    .passingMarks(33.0)
                    .weightagePercentage(100.0)
                    .build();
        });

        Long currentUserId = getCurrentUserId();
        List<StudentMarkItemDto> resultDtos = new ArrayList<>();

        for (StudentMarkItemDto item : request.getMarksList()) {
            AcademicExamStudentMark record;
            if (item.getId() != null) {
                record = studentMarkRepository.findByIdAndIsDeletedFalse(item.getId())
                        .orElse(new AcademicExamStudentMark());
            } else {
                Optional<AcademicExamStudentMark> existing = studentMarkRepository
                        .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndStudentIdAndIsDeletedFalse(
                                examId, classId, sectionId, subjectId, item.getStudentId());
                record = existing.orElse(new AcademicExamStudentMark());
            }

            if (record.getId() == null) {
                record.setExamId(examId);
                record.setClassId(classId);
                record.setSectionId(sectionId);
                record.setSubjectId(subjectId);
                record.setStudentId(item.getStudentId());
                record.setCreatedBy(currentUserId);
                record.setCreatedOn(IstClock.nowDateTime());
                record.setIsDeleted(false);
            } else {
                record.setModifiedBy(currentUserId);
                record.setModifiedAt(IstClock.nowDateTime());
            }

            record.setRollNo(item.getRollNo());
            record.setStudentName(item.getStudentName());

            Map<String, Double> cMarks = (item.getComponentMarks() != null && !item.getComponentMarks().isEmpty())
                    ? new HashMap<>(item.getComponentMarks())
                    : new HashMap<>();

            if (!cMarks.containsKey("Theory") && item.getTheoryMarks() != null) cMarks.put("Theory", item.getTheoryMarks());
            if (!cMarks.containsKey("Practical") && item.getPracticalMarks() != null) cMarks.put("Practical", item.getPracticalMarks());
            if (!cMarks.containsKey("Internal") && item.getInternalMarks() != null) cMarks.put("Internal", item.getInternalMarks());

            record.setComponentMarks(cMarks);
            record.setTheoryMarks(cMarks.get("Theory"));
            record.setPracticalMarks(cMarks.get("Practical"));
            record.setInternalMarks(cMarks.get("Internal"));

            String att = item.getAttendanceStatus() != null ? item.getAttendanceStatus().trim().toUpperCase() : "PRESENT";
            record.setAttendanceStatus(att);
            record.setRemarks(item.getRemarks());
            record.setEntryStatus(targetStatus);

            Double totalSum = null;
            if (!cMarks.isEmpty()) {
                double s = 0.0;
                for (Double v : cMarks.values()) {
                    if (v != null) s += v;
                }
                totalSum = s;
            }

            // Auto-calculate total, percentage, grade, pass/fail
            GradeResult gr = calculateGradeAndResult(
                    record.getTheoryMarks(), record.getPracticalMarks(), record.getInternalMarks(), totalSum,
                    matrix.getTotalMaxMarks(), matrix.getPassingMarks(), att);

            record.setTotalMarks(gr.total);
            record.setPercentage(gr.percentage);
            record.setGrade(gr.grade);
            record.setResult(gr.result);

            AcademicExamStudentMark saved = studentMarkRepository.save(record);

            Map<String, String> cDisplays = new HashMap<>();
            for (Map.Entry<String, Double> entry : cMarks.entrySet()) {
                cDisplays.put(entry.getKey(), formatDisplay(entry.getValue(), att, entry.getKey()));
            }

            resultDtos.add(StudentMarkItemDto.builder()
                    .id(saved.getId())
                    .studentId(saved.getStudentId())
                    .rollNo(saved.getRollNo())
                    .studentName(saved.getStudentName())
                    .theoryMarks(saved.getTheoryMarks())
                    .practicalMarks(saved.getPracticalMarks())
                    .internalMarks(saved.getInternalMarks())
                    .componentMarks(cMarks)
                    .componentDisplays(cDisplays)
                    .theoryDisplay(formatDisplay(saved.getTheoryMarks(), saved.getAttendanceStatus(), "THEORY"))
                    .practicalDisplay(formatDisplay(saved.getPracticalMarks(), saved.getAttendanceStatus(), "PRACTICAL"))
                    .internalDisplay(formatDisplay(saved.getInternalMarks(), saved.getAttendanceStatus(), "INTERNAL"))
                    .totalMarks(saved.getTotalMarks())
                    .percentage(saved.getPercentage())
                    .attendanceStatus(saved.getAttendanceStatus())
                    .grade(saved.getGrade())
                    .result(saved.getResult())
                    .remarks(saved.getRemarks())
                    .build());
        }

        // Update submission tracking record
        AcademicExamMarksSubmission sub = existingSub.orElse(AcademicExamMarksSubmission.builder()
                .examId(examId)
                .classId(classId)
                .sectionId(sectionId)
                .subjectId(subjectId)
                .build());

        sub.setStatus(targetStatus);
        sub.setModifiedBy(currentUserId);
        sub.setModifiedAt(IstClock.nowDateTime());
        if ("SUBMITTED".equals(targetStatus)) {
            sub.setSubmittedByUser(currentUserId);
            sub.setSubmittedAt(IstClock.nowDateTime());
        }
        marksSubmissionRepository.save(sub);

        String className = getMasterName(classId.intValue());
        String sectionName = getMasterName(sectionId.intValue());
        String subjectName = getMasterName(subjectId.intValue());
        AcademicExam exam1 = academicExamRepository.findByIdAndIsDeletedFalse(examId).orElse(null);

        MarksGridLoadResponse response = MarksGridLoadResponse.builder()
                .examId(examId)
                .examName(exam1 != null ? exam1.getExamName() : null)
                .classId(classId)
                .className(className)
                .sectionId(sectionId)
                .sectionName(sectionName)
                .subjectId(subjectId)
                .subjectName(subjectName)
                .assessmentComponent(request.getAssessmentComponent() != null ? request.getAssessmentComponent() : "Theory")
                .assessmentComponents(exam != null && exam.getAssessmentComponents() != null ? exam.getAssessmentComponents() : List.of("Theory", "Practical", "Internal"))
                .componentMaxMarks(matrix.getComponentMaxMarks())
                .theoryMaxMarks(matrix.getTheoryMaxMarks())
                .practicalMaxMarks(matrix.getPracticalMaxMarks())
                .internalMaxMarks(matrix.getInternalMaxMarks())
                .totalMaxMarks(matrix.getTotalMaxMarks())
                .passingMarks(matrix.getPassingMarks())
                .weightagePercentage(matrix.getWeightagePercentage())
                .submissionStatus(targetStatus)
                .isLocked("LOCKED".equalsIgnoreCase(targetStatus))
                .students(resultDtos)
                .build();

        String msg = "DRAFT".equals(targetStatus) ? "Marks draft saved successfully" : "Marks submitted successfully";
        return StandardResponse.success(response, msg);
    }

    private static class GradeResult {
        Double total;
        Double percentage;
        String grade;
        String result;

        GradeResult(Double total, Double percentage, String grade, String result) {
            this.total = total;
            this.percentage = percentage;
            this.grade = grade;
            this.result = result;
        }
    }

    private static GradeResult calculateGradeAndResult(
            Double theory, Double practical, Double internal, Double totalOverride, Double totalMaxMarks, Double passingMarks, String attendanceStatus) {
        if ("AB".equalsIgnoreCase(attendanceStatus) || "ABSENT".equalsIgnoreCase(attendanceStatus)) {
            return new GradeResult(null, null, "-", "Absent");
        }
        if ("EX".equalsIgnoreCase(attendanceStatus) || "EXEMPT".equalsIgnoreCase(attendanceStatus)) {
            return new GradeResult(null, null, "-", "Exempt");
        }
        if ("NA".equalsIgnoreCase(attendanceStatus) || "NOT_APPLICABLE".equalsIgnoreCase(attendanceStatus)) {
            return new GradeResult(null, null, "-", "Not Applicable");
        }

        double total;
        if (totalOverride != null) {
            total = totalOverride;
        } else {
            double t = theory != null ? theory : 0.0;
            double p = practical != null ? practical : 0.0;
            double i = internal != null ? internal : 0.0;
            total = t + p + i;
        }
        double max = (totalMaxMarks != null && totalMaxMarks > 0) ? totalMaxMarks : 100.0;
        double pct = (total * 100.0) / max;

        String grade;
        if (pct >= 90.0) grade = "A+";
        else if (pct >= 80.0) grade = "A";
        else if (pct >= 70.0) grade = "B";
        else if (pct >= 50.0) grade = "C";
        else if (pct >= 33.0) grade = "D";
        else grade = "F";

        double passMark = passingMarks != null ? passingMarks : 33.0;
        String result = total >= passMark ? "Pass" : "Fail";

        return new GradeResult(total, Math.round(pct * 10.0) / 10.0, grade, result);
    }

    @Override
    @Transactional
    public StandardResponse<Map<String, Object>> lockMarks(Long examId, Long classId, Long sectionId, Long subjectId) {
        Long currentUserId = getCurrentUserId();
        AcademicExamMarksSubmission sub = marksSubmissionRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(examId, classId, sectionId, subjectId)
                .orElse(AcademicExamMarksSubmission.builder()
                        .examId(examId)
                        .classId(classId)
                        .sectionId(sectionId)
                        .subjectId(subjectId)
                        .build());

        sub.setStatus("LOCKED");
        sub.setLockedByUser(currentUserId);
        sub.setLockedAt(IstClock.nowDateTime());
        sub.setModifiedBy(currentUserId);
        sub.setModifiedAt(IstClock.nowDateTime());
        marksSubmissionRepository.save(sub);

        // Update all student records to LOCKED
        List<AcademicExamStudentMark> marks = studentMarkRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalseOrderByRollNoAsc(examId, classId, sectionId, subjectId);
        for (AcademicExamStudentMark m : marks) {
            m.setEntryStatus("LOCKED");
            m.setModifiedBy(currentUserId);
            m.setModifiedAt(IstClock.nowDateTime());
        }
        studentMarkRepository.saveAll(marks);

        return StandardResponse.success(
                Map.of("examId", examId, "status", "LOCKED", "lockedAt", sub.getLockedAt().toString()),
                "Marks sheet locked successfully. Further edits are disabled.");
    }

    @Override
    @Transactional
    public StandardResponse<Map<String, Object>> unlockMarks(MarksUnlockRequest request) {
        Long currentUserId = getCurrentUserId();
        AcademicExamMarksSubmission sub = marksSubmissionRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalse(
                        request.getExamId(), request.getClassId(), request.getSectionId(), request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("No marks submission found to unlock"));

        sub.setStatus("DRAFT");
        sub.setUnlockedByUser(currentUserId);
        sub.setUnlockedAt(IstClock.nowDateTime());
        sub.setUnlockReason(request.getReason());
        sub.setModifiedBy(currentUserId);
        sub.setModifiedAt(IstClock.nowDateTime());
        marksSubmissionRepository.save(sub);

        // Update student records back to DRAFT to allow editing
        List<AcademicExamStudentMark> marks = studentMarkRepository
                .findByExamIdAndClassIdAndSectionIdAndSubjectIdAndIsDeletedFalseOrderByRollNoAsc(
                        request.getExamId(), request.getClassId(), request.getSectionId(), request.getSubjectId());
        for (AcademicExamStudentMark m : marks) {
            m.setEntryStatus("DRAFT");
            m.setModifiedBy(currentUserId);
            m.setModifiedAt(IstClock.nowDateTime());
        }
        studentMarkRepository.saveAll(marks);

        return StandardResponse.success(
                Map.of("examId", request.getExamId(), "status", "DRAFT", "unlockedAt", sub.getUnlockedAt().toString()),
                "Marks sheet unlocked successfully for corrections. Reason: " + request.getReason());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 4: Export Excel
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportMarksExcel(Long examId, Long classId, Long sectionId, Long subjectId) {
        StandardResponse<MarksGridLoadResponse> gridRes = loadMarksGrid(examId, classId, sectionId, subjectId, "ALL");
        MarksGridLoadResponse data = gridRes.getData();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Marks Entry");

            // Header Style
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Title Row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MARKS ENTRY – " + data.getExamName() + " | Class: " + data.getClassName()
                    + " - " + data.getSectionName() + " | Subject: " + data.getSubjectName());
            CellStyle titleStyle = wb.createCellStyle();
            Font tFont = wb.createFont();
            tFont.setBold(true);
            tFont.setFontHeightInPoints((short) 12);
            titleStyle.setFont(tFont);
            titleCell.setCellStyle(titleStyle);

            List<String> compNames = data.getAssessmentComponents();
            if (compNames == null || compNames.isEmpty()) {
                compNames = List.of("Theory", "Practical", "Internal");
            }

            Map<String, Double> compMax = data.getComponentMaxMarks() != null ? data.getComponentMaxMarks() : Collections.emptyMap();

            List<String> headerCols = new ArrayList<>();
            headerCols.add("#");
            headerCols.add("Roll No");
            headerCols.add("Student Name");

            for (String comp : compNames) {
                Double maxVal = compMax.get(comp);
                if (maxVal == null) {
                    if ("Theory".equalsIgnoreCase(comp)) maxVal = data.getTheoryMaxMarks();
                    else if ("Practical".equalsIgnoreCase(comp)) maxVal = data.getPracticalMaxMarks();
                    else if ("Internal".equalsIgnoreCase(comp)) maxVal = data.getInternalMaxMarks();
                    else maxVal = 0.0;
                }
                headerCols.add(comp + " (" + String.format("%.0f", maxVal != null ? maxVal : 0.0) + ")");
            }

            headerCols.add("Total (" + String.format("%.0f", data.getTotalMaxMarks() != null ? data.getTotalMaxMarks() : 100.0) + ")");
            headerCols.add("Grade");
            headerCols.add("Result");
            headerCols.add("Remarks");

            Row hRow = sheet.createRow(2);
            for (int i = 0; i < headerCols.size(); i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(headerCols.get(i));
                c.setCellStyle(headerStyle);
            }

            // Data Rows
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle leftStyle = wb.createCellStyle();
            leftStyle.setAlignment(HorizontalAlignment.LEFT);

            int rIdx = 3;
            int counter = 1;
            for (StudentMarkItemDto s : data.getStudents()) {
                Row row = sheet.createRow(rIdx++);
                int cCol = 0;
                row.createCell(cCol++).setCellValue(counter++);
                row.createCell(cCol++).setCellValue(s.getRollNo() != null ? s.getRollNo() : "-");

                Cell nameCell = row.createCell(cCol++);
                nameCell.setCellValue(s.getStudentName());
                nameCell.setCellStyle(leftStyle);

                Map<String, String> displays = s.getComponentDisplays() != null ? s.getComponentDisplays() : Collections.emptyMap();
                for (String comp : compNames) {
                    String dVal = displays.get(comp);
                    if (dVal == null) {
                        if ("Theory".equalsIgnoreCase(comp)) dVal = s.getTheoryDisplay();
                        else if ("Practical".equalsIgnoreCase(comp)) dVal = s.getPracticalDisplay();
                        else if ("Internal".equalsIgnoreCase(comp)) dVal = s.getInternalDisplay();
                    }
                    row.createCell(cCol++).setCellValue(dVal != null ? dVal : "-");
                }

                row.createCell(cCol++).setCellValue(s.getTotalMarks() != null ? String.format("%.0f", s.getTotalMarks()) : "-");
                row.createCell(cCol++).setCellValue(s.getGrade() != null ? s.getGrade() : "-");
                row.createCell(cCol++).setCellValue(s.getResult() != null ? s.getResult() : "-");
                row.createCell(cCol++).setCellValue(s.getRemarks() != null ? s.getRemarks() : "");
            }

            for (int i = 0; i < headerCols.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to export marks excel", e);
            throw new RuntimeException("Failed to export marks excel", e);
        }
    }
}

