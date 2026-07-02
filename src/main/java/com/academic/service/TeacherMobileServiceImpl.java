package com.academic.service;

import com.academic.config.UserContext;
import com.academic.dto.mobile.*;
import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.response.StandardResponse;
import com.academic.utility.IstClock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherMobileServiceImpl implements TeacherMobileService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TimeSlotSubjectMapperRepository slotMapperRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final StudentAttendanceRepository studentAttendanceRepository;
    private final AcademicCalendarEventRepository eventRepository;
    private final ClassSectionRepository classSectionRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final StaffPunchLogRepository staffPunchLogRepository;
    private final SessionRepository sessionRepository;
    private final StudentPromotionMapperRepository studentPromotionMapperRepository;
    private final TimeTableRepository timeTableRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final ExamSetupRepository examSetupRepository;
    private final ExamSubjectConfigRepository examSubjectConfigRepository;
    private final MarksheetRepository marksheetRepository;
    private final MarksheetSubjectRepository marksheetSubjectRepository;
    private final MarksheetSubjectComponentRepository marksheetSubjectComponentRepository;
    private final ExamComponentMasterRepostory componentMasterRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;

    @Override
    public StandardResponse<?> getDashboardData(String employeeId) {
        TeacherAssignment teacher = teacherAssignmentRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new RuntimeException("Teacher assignment not found"));

        LocalDate today = IstClock.today();

        Optional<StaffPunchLog> punchLogOpt = staffPunchLogRepository.findByStaffIdAndWorkDate(
                Integer.valueOf(teacher.getEmployeeId()),
                today.toString());
        TeacherDashboardResponse.PunchStatusResponse punchStatus = null;

        if (punchLogOpt.isPresent()) {
            StaffPunchLog punchLog = punchLogOpt.get();
            String status;
            if (punchLog.getPunchOutTime() == null) {
                status = "PUNCH_IN";
            } else {
                status = "PUNCH_OUT";
            }
            String timeStr = punchLog.getPunchInTime() != null
                    ? punchLog.getPunchInTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
                    : "";
            punchStatus = TeacherDashboardResponse.PunchStatusResponse.builder()
                    .status(status)
                    .punchTime(timeStr)
                    .canPunchOut("PUNCH_IN".equals(status))
                    .build();
        }

        Long teacherId = null;
        try {
            teacherId = Long.parseLong(teacher.getEmployeeId()); // user mentioned teacher name stores
            // staff id
        } catch (NumberFormatException e) {
            return StandardResponse.error("Invalid Teacher ID stored in TeacherName", "DATA_ERROR", null);
        }

        List<TeacherDashboardResponse.TeacherScheduleSlotDto> schedule = getTeacherSchedule(teacherId);
        int totalClasses = schedule.size();
        long markedCount = schedule.stream()
                .filter(TeacherDashboardResponse.TeacherScheduleSlotDto::isAttendanceMarked)
                .count();
        String attDone = totalClasses > 0 ? (markedCount * 100 / totalClasses) + "% Done" : "0% Done";

        List<TeacherDashboardResponse.AnnouncementDto> announcements = eventRepository.findAll().stream()
                .limit(5)
                .map(e -> TeacherDashboardResponse.AnnouncementDto.builder()
                        .type(e.getType())
                        .title(e.getEventName())
                        .content(null)
                        .date(e.getDate().toString())
                        .build())
                .collect(Collectors.toList());

        return StandardResponse.success(TeacherDashboardResponse.builder()
                .teacherName(teacher.getTeacherName())
                .date(today.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")))
                .punchStatus(punchStatus)
                .summary(TeacherDashboardResponse.DailySummaryResponse.builder()
                        .totalClasses(totalClasses)
                        .attendancePercentage(attDone)
                        .build())
                .announcements(announcements)
                .todaySchedule(schedule)
                .build(), "Dashboard data fetched");
    }

    private List<TeacherDashboardResponse.TeacherScheduleSlotDto> getTeacherSchedule(Long teacherId) {
        LocalDate today = IstClock.today();
        int dayOfWeek = today.getDayOfWeek().getValue();
        LocalTime now = IstClock.nowTime();

        List<TimeSlotSubjectMapper> mapperSlots = slotMapperRepository.findByTeacherId(teacherId);
        List<TeacherDashboardResponse.TeacherScheduleSlotDto> slots = new ArrayList<>();

        for (TimeSlotSubjectMapper s : mapperSlots) {
            if (s.getDay() != null && s.getDay() == dayOfWeek) {
                TimeTable tt = s.getTimeTable();
                if (tt == null || Boolean.TRUE.equals(tt.getIsDeleted())) {
                    continue;
                }

                String subName = "Unknown";
                if (s.getSubjectId() != null) {
                    subName = subjectRepository.findById(s.getSubjectId().longValue())
                            .map(Subject::getSubjectName)
                            .orElse("Unknown");
                }
                String clsName = "Class " + tt.getClassId();
                if (tt.getClassId() != null) {
                    clsName = classSectionRepository.findById(tt.getClassId())
                            .map(cs -> getName(cs.getClassId()))
                            .orElse("Class " + tt.getClassId());
                }

                LocalTime start = null;
                LocalTime end = null;
                try {
                    start = LocalTime.parse(s.getStartTime());
                    end = LocalTime.parse(s.getEndTime());
                } catch (Exception e) {
                    continue;
                }

                String status = now.isAfter(start) && now.isBefore(end) ? "ONGOING"
                        : (now.isAfter(end) ? "COMPLETED" : "UPCOMING");

                List<Long> sIds = new ArrayList<>();
                if (tt.getClassId() != null && tt.getSectionId() != null) {
                    sIds = studentRepository
                            .findByClassApplyingForAndSection(
                                    tt.getClassId().intValue(),
                                    tt.getSectionId().intValue())
                            .stream().map(st -> Long.valueOf(st.getId()))
                            .collect(Collectors.toList());
                }
                boolean marked = !sIds.isEmpty()
                        && studentAttendanceRepository
                        .existsByStudentIdInAndAttendanceDate(sIds,
                                today);

                String timeRange = s.getStartTime() + " - " + s.getEndTime();
                try {
                    java.time.format.DateTimeFormatter outputFormat = java.time.format.DateTimeFormatter
                            .ofPattern("hh:mm a");
                    if (s.getStartTime() != null && s.getEndTime() != null) {
                        timeRange = LocalTime.parse(s.getStartTime()).format(outputFormat)
                                + " - " +
                                LocalTime.parse(s.getEndTime()).format(outputFormat);
                    }
                } catch (Exception ignored) {
                }

                slots.add(TeacherDashboardResponse.TeacherScheduleSlotDto.builder()
                        .id(s.getId()).subjectName(subName)
                        .classSection(clsName + (tt.getSectionId() != null
                                ? "-" + tt.getSectionId()
                                : ""))
                        .timeRange(timeRange)
                        .room(s.getRoom()).status(status)
                        .attendanceMarked(marked).build());
            }
        }
        return slots.stream().sorted((s1, s2) -> s1.getTimeRange().compareTo(s2.getTimeRange()))
                .collect(Collectors.toList());
    }

    private String getName(Integer id) {
        if (id == null)
            return null;
        return commonMasterRepository.findByIdAndStatusTrue(id).map(CommonMaster::getData).orElse("Unknown");
    }

    @Override
    public StandardResponse<?> getAttendanceList(Long staffId, LocalDate date, Long classId, Long sectionId) {
        Long targetClassId;
        Long targetSectionId;

        if (classId != null && sectionId != null) {
            targetClassId = classId;
            targetSectionId = sectionId;
        } else {
            if (staffId == null) {
                return StandardResponse.error("Staff ID not found", "ID_MISSING", null);
            }

            // 1. Fetch the record from the teacherAssignment table first to get the class &
            // section of teacher
            ClassSection assignment = classSectionRepository
                    .findByClassTeacherIdAndIsDeletedFalse(staffId)
                    .orElseThrow(() -> new RuntimeException(
                            "Teacher assignment not found for staff ID: " + staffId));

            if (assignment.getClassId() == null || assignment.getSection() == null) {
                return StandardResponse.error(
                        "Teacher is not assigned to any specific class and section for attendance",
                        "ASSIGNMENT_INCOMPLETE", null);
            }

            targetClassId = assignment.getClassId().longValue();
            targetSectionId = assignment.getSection().longValue();
        }

        // 2. Fetch students for that section for that day
        String sessionText = StudentMobileServiceImpl.getCurrentSession();
        Session session = sessionRepository.findBySession(sessionText);
        if (session == null) {
            session = sessionRepository.findByIsActiveTrue().orElseThrow(() -> new RuntimeException("Session " + sessionText
                            + " not found and no active session fallback"));
        }

        List<StudentPromotionMapper> activePromotions = studentPromotionMapperRepository
                .findByToClassAndToSectionAndAcademicYear(targetClassId.intValue(),
                        targetSectionId.intValue(),
                        session.getId());

        List<Integer> studentIds = activePromotions.stream().map(StudentPromotionMapper::getStudentId)
                .collect(Collectors.toList());

        List<Student> initialStudents = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            initialStudents = studentRepository.findAllById(studentIds);
            // Just sorting them by ID or Name could be good, but we just use the fetched
            // list
        }
        final List<Student> students = initialStudents;

        List<TeacherAttendanceListResponse.StudentAttendanceDto> dtoList = students.stream().map(s -> {
            Optional<StudentAttendance> att = studentAttendanceRepository
                    .findByStudentIdAndAttendanceDate(Long.valueOf(s.getId()), date);
            return TeacherAttendanceListResponse.StudentAttendanceDto.builder()
                    .studentId(Long.valueOf(s.getId()))
                    .studentName(s.getFirstName() + " " + s.getLastName())
                    .rollNo(String.format("%02d", students.indexOf(s) + 1))
                    .status(att.map(a -> a.getStatus().name()).orElse("NOT_MARKED")).build();
        }).collect(Collectors.toList());

        long p = dtoList.stream().filter(d -> "PRESENT".equals(d.getStatus())).count();
        long a = dtoList.stream().filter(d -> "ABSENT".equals(d.getStatus())).count();

        String className = getName(targetClassId.intValue());
        String secName = getName(targetSectionId.intValue());
        String classSectionName = (className != null ? className : "Unknown") + "-"
                + (secName != null ? secName : "Unknown");

        return StandardResponse.success(TeacherAttendanceListResponse.builder()
                .classSectionName(classSectionName).date(date.toString()).students(dtoList)
                .summary(TeacherAttendanceListResponse.AttendanceSummary.builder().total(dtoList.size())
                        .present((int) p).absent((int) a).build())
                .build(), "Attendance list fetched");
    }

    @Override
    @Transactional
    public StandardResponse<?> submitAttendance(AttendanceSubmissionRequest request) {
        for (AttendanceSubmissionRequest.StudentStatus item : request.getAttendanceList()) {
            StudentAttendance att = studentAttendanceRepository
                    .findByStudentIdAndAttendanceDate(item.getStudentId(), request.getDate())
                    .orElse(new StudentAttendance());
            att.setStudentId(item.getStudentId());
            att.setAttendanceDate(request.getDate());
            att.setStatus(StudentAttendance.AttendanceStatus.valueOf(item.getStatus()));
            studentAttendanceRepository.save(att);
        }
        return StandardResponse.success(null, "Attendance submitted");
    }

    @Override
    public StandardResponse<?> getExamSchedule(Long staffId) {
        TeacherAssignment assignment = teacherAssignmentRepository
                .findByEmployeeIdAndIsDeletedFalse(staffId.toString())
                .orElseThrow(() -> new RuntimeException("Teacher assignment not found"));

        if (assignment.getClassId() == null) {
            return StandardResponse.error("No class assigned to teacher", "NO_CLASS", null);
        }

        Session session = getActiveSession();
        List<ExamSchedule> schedules = examScheduleRepository.findBySession_IdAndIsActiveTrue(session.getId());

        if (schedules.isEmpty()) {
            return StandardResponse.success(new ArrayList<>(), "No active exam schedule found");
        }

        // Taking the first active one for simplicity
        ExamSchedule currentExam = schedules.get(0);

        List<ExamSubjectConfig> configs = examSubjectConfigRepository
                .findAllWithFilters(session.getId(), currentExam.getExamType().getId(),
                        assignment.getClassId().intValue());

        List<TeacherExamScheduleResponse.ExamSlotDto> slots = configs.stream().map(config -> {
            String subName = config.getSubject() != null ? config.getSubject().getSubjectName() : "Unknown";
            String subCode = config.getSubject() != null ? config.getSubject().getSubjectCode() : "";

            String dateStr = currentExam.getStartDate() != null
                    ? currentExam.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, EEEE"))
                    : "TBD";

            String status = "UPCOMING";
            LocalDate today = IstClock.today();
            if (currentExam.getEndDate() != null && today.isAfter(currentExam.getEndDate())) {
                status = "COMPLETED";
            } else if (currentExam.getStartDate() != null && (today.isEqual(currentExam.getStartDate())
                    || today.isAfter(currentExam.getStartDate()))) {
                status = "ONGOING";
            }

            String timeRange = "TBD";
            if (currentExam.getStartDate() != null && currentExam.getEndDate() != null) {
                DateTimeFormatter rangeFormat = DateTimeFormatter.ofPattern("dd MMM");
                timeRange = currentExam.getStartDate().format(rangeFormat) + " - "
                        + currentExam.getEndDate().format(rangeFormat);
            }

            return TeacherExamScheduleResponse.ExamSlotDto.builder()
                    .date(dateStr)
                    .subjectId(config.getSubject().getId())
                    .subjectName(subName)
                    .subjectCode(subCode)
                    .timeRange(timeRange)
                    .location("Hall TBD")
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        return StandardResponse.success(TeacherExamScheduleResponse.builder()
                .examTitle(currentExam.getExamTitle())
                .subTitle(currentExam.getExamTitle() + " - " + session.getSession())
                .classId(assignment.getClassId().intValue())
                .examTypeId(currentExam.getExamType().getId())
                .exams(slots)
                .build(), "Exam schedule fetched");
    }

    @Override
    public StandardResponse<?> getStudentListForMarks( Integer examTypeId, Long subjectId) {
        Session session = getActiveSession();

        Long staffId = UserContext.getStaffId();

        TeacherAssignment assignment = teacherAssignmentRepository
                .findByEmployeeIdAndIsDeletedFalse(staffId.toString())
                .orElseThrow(() -> new RuntimeException("Teacher assignment not found"));


        List<StudentPromotionMapper> activePromotions = studentPromotionMapperRepository
                .findActivePromotionsByClassAndSection(assignment.getClassId().intValue(), assignment.getSectionId().intValue(), session.getId());

        List<Integer> studentIds = activePromotions.stream().map(StudentPromotionMapper::getStudentId)
                .collect(Collectors.toList());

        List<Student> students = studentRepository.findAllById(studentIds);

        Optional<ExamSubjectConfig> configOpt = examSubjectConfigRepository
                .findBySession_IdAndExamType_IdAndClassId_IdAndSubject_IdAndIsDeleteFalse(
                        session.getId(), examTypeId, assignment.getClassId().intValue(), subjectId);

        if (configOpt.isEmpty()) {
            return StandardResponse.error("Subject configuration not found", "CONFIG_NOT_FOUND", null);
        }

        ExamSubjectConfig config = configOpt.get();
        List<EnterMarksResponse.ComponentDto> components = config.getComponents().stream()
                .map(c -> EnterMarksResponse.ComponentDto.builder()
                        .componentId(c.getComponent().getId())
                        .componentName(c.getComponent().getComponentName())
                        .maxMarks(c.getMaxMarks())
                        .build())
                .collect(Collectors.toList());

        List<EnterMarksResponse.StudentMarkListDto> dtoList = students.stream().map(s -> {
            Optional<Marksheet> sheetOpt = marksheetRepository
                    .findByStudentIdAndClassIdAndExamTypeIdAndSessionIdAndIsDeletedFalse(
                            s.getId(), assignment.getClassId().intValue(), examTypeId, session.getId());

            List<EnterMarksResponse.StudentComponentMarkDto> studentCompMarks = new ArrayList<>();
            Integer totalObtained = 0;

            if (sheetOpt.isPresent()) {
                Optional<MarksheetSubject> subMarksOpt = marksheetSubjectRepository
                        .findByMarksheet_IdAndSubjectId(sheetOpt.get().getId(),
                                subjectId.intValue());
                if (subMarksOpt.isPresent()) {
                    MarksheetSubject subMarks = subMarksOpt.get();
                    totalObtained = subMarks.getTotalMarks();
                    studentCompMarks = subMarks.getComponents().stream()
                            .map(c -> EnterMarksResponse.StudentComponentMarkDto.builder()
                                    .componentId(c.getComponent().getId())
                                    .marksObtained(c.getMarksObtained())
                                    .build())
                            .collect(Collectors.toList());
                }
            }

            return EnterMarksResponse.StudentMarkListDto.builder()
                    .studentId(Long.valueOf(s.getId()))
                    .studentName(s.getFirstName() + " " + s.getLastName())
                    .rollNo(String.format("%02d", students.indexOf(s) + 1))
                    .componentMarks(studentCompMarks)
                    .marksObtained(totalObtained)
                    .build();
        }).collect(Collectors.toList());

        return StandardResponse.success(EnterMarksResponse.builder()
                .className(getName(assignment.getClassId().intValue()))
                .examType(getName(examTypeId))
                .subjectName(subjectRepository.findById(subjectId).map(Subject::getSubjectName)
                        .orElse("Unknown"))
                .components(components)
                .students(dtoList)
                .build(), "Student list for marks fetched");
    }

    @Override
    @Transactional
    public StandardResponse<?> saveMarks(EnterMarksRequest request) {
        Session session = getActiveSession();

        Optional<ExamSubjectConfig> configOpt = examSubjectConfigRepository
                .findBySession_IdAndExamType_IdAndClassId_IdAndSubject_IdAndIsDeleteFalse(
                        session.getId(), request.getExamTypeId(), request.getClassId(),
                        request.getSubjectId());

        if (configOpt.isEmpty()) {
            return StandardResponse.error("Subject configuration not found", "CONFIG_NOT_FOUND", null);
        }

        ExamSubjectConfig config = configOpt.get();

        for (EnterMarksRequest.StudentMarkDto dto : request.getMarks()) {
            Marksheet sheet = marksheetRepository
                    .findByStudentIdAndClassIdAndExamTypeIdAndSessionIdAndIsDeletedFalse(
                            dto.getStudentId().intValue(), request.getClassId(),
                            request.getExamTypeId(), session.getId())
                    .orElse(Marksheet.builder()
                            .studentId(dto.getStudentId().intValue())
                            .classId(request.getClassId())
                            .examTypeId(request.getExamTypeId())
                            .sessionId(session.getId())
                            .examDate(IstClock.today())
                            .published(false)
                            .isDeleted(false)
                            .subjects(new ArrayList<>())
                            .build());

            if (sheet.getCreatedAt() == null) {
                sheet.setCreatedAt(IstClock.nowDateTime());
            }

            marksheetRepository.save(sheet);

            MarksheetSubject subMarks = marksheetSubjectRepository
                    .findByMarksheet_IdAndSubjectId(sheet.getId(),
                            request.getSubjectId().intValue())
                    .orElse(MarksheetSubject.builder()
                            .marksheet(sheet)
                            .subjectId(request.getSubjectId().intValue())
                            .components(new ArrayList<>())
                            .build());

            // Clear existing components to update
            subMarks.getComponents().clear();

            int totalObtained = 0;
            int totalMax = 0;

            if (dto.getComponentMarks() != null && !dto.getComponentMarks().isEmpty()) {
                for (EnterMarksRequest.ComponentMarkDto cmd : dto.getComponentMarks()) {
                    ExamComponentMaster component = componentMasterRepository
                            .findById(cmd.getComponentId())
                            .orElseThrow(() -> new RuntimeException("Component not found"));

                    ExamSubjectConfigComponent configComp = config.getComponents().stream()
                            .filter(c -> c.getComponent().getId()
                                    .equals(cmd.getComponentId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "Component not configured for this subject"));

                    MarksheetSubjectComponent msc = MarksheetSubjectComponent.builder()
                            .subject(subMarks)
                            .component(component)
                            .marksObtained(cmd.getMarksObtained())
                            .maxMarks(configComp.getMaxMarks())
                            .build();

                    subMarks.getComponents().add(msc);
                    totalObtained += safeMarks(cmd.getMarksObtained());
                    totalMax += safeMarks(configComp.getMaxMarks());
                }
            } else {
                // Fallback logic for theory/practical/internal if componentMarks is empty
                // This assumes existing component names match these labels
                mapHistoricalMarks(dto, subMarks, config);
                totalObtained = subMarks.getComponents().stream()
                        .mapToInt(c -> safeMarks(c.getMarksObtained())).sum();
                totalMax = subMarks.getComponents().stream().mapToInt(c -> safeMarks(c.getMaxMarks()))
                        .sum();
            }

            subMarks.setTotalMarks(totalObtained);
            subMarks.setTotalMax(totalMax);
            subMarks.setSubjectRemarks(dto.getRemarks());

            if (!sheet.getSubjects().contains(subMarks)) {
                sheet.getSubjects().add(subMarks);
            }

            marksheetSubjectRepository.save(subMarks);

            // Update sheet totals
            int sheetTotalObtained = sheet.getSubjects().stream()
                    .mapToInt(s -> safeMarks(s.getTotalMarks())).sum();
            int sheetTotalMax = sheet.getSubjects().stream()
                    .mapToInt(s -> safeMarks(s.getTotalMax())).sum();

            sheet.setTotalMarksObtained(sheetTotalObtained);
            sheet.setTotalMaxMarks(sheetTotalMax);
            if (sheetTotalMax > 0) {
                sheet.setPercentage((double) sheetTotalObtained * 100 / sheetTotalMax);
            }
            marksheetRepository.save(sheet);
        }

        return StandardResponse.success(null, "Marks saved successfully");
    }

    private void mapHistoricalMarks(EnterMarksRequest.StudentMarkDto dto, MarksheetSubject subMarks,
                                    ExamSubjectConfig config) {
        // Heuristic mapping of theory/practical/internal to configured components
        for (ExamSubjectConfigComponent esc : config.getComponents()) {
            String name = esc.getComponent().getComponentName().toUpperCase();
            Integer marks = null;
            if (name.contains("THEORY"))
                marks = dto.getTheoryMarks();
            else if (name.contains("PRACTICAL"))
                marks = dto.getPracticalMarks();
            else if (name.contains("INTERNAL"))
                marks = dto.getInternalMarks();

            if (marks != null) {
                subMarks.getComponents().add(MarksheetSubjectComponent.builder()
                        .subject(subMarks)
                        .component(esc.getComponent())
                        .marksObtained(marks)
                        .maxMarks(esc.getMaxMarks())
                        .build());
            }
        }
    }

    private int safeMarks(Integer v) {
        return v == null ? 0 : v;
    }

    @Override
    @Transactional
    public StandardResponse<?> applyLeave(Long staffId, LeaveSubmissionRequest request) {
        LeaveApplication leave = LeaveApplication.builder()
                .employeeId(staffId)
                .leaveType(request.getLeaveType())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .reason(request.getReason())
                .attachmentPath(request.getAttachmentUrl())
                .status("PENDING")
                .createdAt(IstClock.nowDateTime())
                .isDelete(false)
                .applicationNo("LV-" + System.currentTimeMillis())
                .build();

        leaveApplicationRepository.save(leave);
        return StandardResponse.success(null, "Leave application submitted");
    }

    @Override
    public StandardResponse<?> getLeaveHistory(Long staffId) {
        List<LeaveApplication> history = leaveApplicationRepository
                .findByEmployeeIdAndIsDeleteFalseOrderByFromDateDesc(staffId);

        List<TeacherLeaveHistoryResponse.LeaveItemDto> dtoList = history.stream().map(l -> {
            long days = java.time.temporal.ChronoUnit.DAYS.between(l.getFromDate(), l.getToDate()) + 1;
            String dateRange = l.getFromDate().format(DateTimeFormatter.ofPattern("dd MMM")) + " - " +
                    l.getToDate().format(DateTimeFormatter.ofPattern("dd MMM")) + " (" + days
                    + " Days)";

            return TeacherLeaveHistoryResponse.LeaveItemDto.builder()
                    .leaveType(l.getLeaveType())
                    .dateRange(dateRange)
                    .reason(l.getReason())
                    .status(l.getStatus())
                    .rejectionReason(l.getLastActionComments())
                    .build();
        }).collect(Collectors.toList());

        return StandardResponse.success(TeacherLeaveHistoryResponse.builder().history(dtoList).build(),
                "Leave history fetched");
    }

    private Session getActiveSession() {
        String sessionText = StudentMobileServiceImpl.getCurrentSession();
        Session session = sessionRepository.findBySession(sessionText);
        if (session == null) {
            session = sessionRepository.findByIsActiveTrue()
                    .orElseThrow(() -> new RuntimeException("No active session found"));
        }
        return session;
    }
}
