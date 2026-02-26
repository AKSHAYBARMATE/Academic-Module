package com.academic.service;

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
                        if (s.getDay() != null && s.getDay() == dayOfWeek && Boolean.TRUE.equals(s.getActive())) {
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
        public StandardResponse<?> getAttendanceList(Long staffId, LocalDate date) {
                if (staffId == null) {
                        return StandardResponse.error("Staff ID not found", "ID_MISSING", null);
                }

                // 1. Find the currently active slot for this teacher
                LocalTime now = IstClock.nowTime();
                int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Mon ... 7 = Sun

                List<TimeSlotSubjectMapper> teacherSlots = slotMapperRepository.findByTeacherId(staffId);

                TimeSlotSubjectMapper activeSlot = teacherSlots.stream()
                                .filter(s -> s.getDay() != null && s.getDay() == dayOfWeek)
                                .filter(s -> {
                                        try {
                                                LocalTime start = LocalTime.parse(s.getStartTime());
                                                LocalTime end = LocalTime.parse(s.getEndTime());
                                                // Check if 'now' is within start and end
                                                return !now.isBefore(start) && !now.isAfter(end);
                                        } catch (Exception e) {
                                                return false;
                                        }
                                })
                                .findFirst()
                                .orElse(null);

                if (activeSlot == null) {
                        return StandardResponse.error("No ongoing class found at this time for attendance",
                                        "NO_ACTIVE_CLASS", null);
                }

                TimeTable tt = activeSlot.getTimeTable();
                if (tt == null) {
                        return StandardResponse.error("Timetable missing for the active slot", "TIMETABLE_MISSING",
                                        null);
                }

                // 2. Resolve real CommonMaster IDs from ClassSection


                String sessionText = StudentMobileServiceImpl.getCurrentSession();
                Session session = sessionRepository.findBySession(sessionText);
                if (session == null) {
                        session = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("Session " + sessionText
                                                        + " not found and no active session fallback"));
                }

                List<StudentPromotionMapper> activePromotions = studentPromotionMapperRepository
                                .findActivePromotionsByClassAndSection(tt.getClassId().intValue(), tt.getSectionId().intValue(),
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

                String className = getName(tt.getClassId().intValue());
                String secName = getName(tt.getSectionId().intValue());
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
}
