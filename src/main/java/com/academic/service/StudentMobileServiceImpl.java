package com.academic.service;

import com.academic.dto.mobile.*;
import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.response.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentMobileServiceImpl implements StudentMobileService {

        private final StudentRepository studentRepository;
        private final StudentPromotionMapperRepository studentPromotionMapperRepository;

        private final StudentAttendanceRepository attendanceRepository;
        private final TimeTableRepository timeTableRepository;
        private final TimeSlotSubjectMapperRepository slotMapperRepository;
        private final SubjectRepository subjectRepository;
        private final MarksheetRepository marksheetRepository;
        private final MarksheetSubjectMarksRepository subjectMarksRepository;
        private final SessionRepository sessionRepository;
        private final FeeMobileRepository feeMobileRepository;
        private final CommonMasterRepository commonMasterRepository;
        private final TransportRepository transportRepository;

        @Override
        public StandardResponse<?> getDashboardData(Long studentId) {
                Student student = studentRepository.findById(studentId.intValue())
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                // Get Active Session
                String sessionText = getCurrentSession();
                Session session = sessionRepository.findBySession(sessionText);
                if (session == null) {
                        session = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("Session " + sessionText
                                                        + " not found and no active session fallback"));
                }

                // Get Student Promotion Details (Current Class/Section)
                StudentPromotionMapper promotion = studentPromotionMapperRepository
                                .findActivePromotion(student.getId(), session.getId())
                                .orElse(null);

                Integer currentClassId = (promotion != null) ? promotion.getToClass() : student.getClassApplyingFor();
                Integer currentSectionId = (promotion != null) ? promotion.getToSection() : student.getSection();

                // Attendance Summary
                LocalDate today = LocalDate.now();
                LocalDate startOfMonth = today.withDayOfMonth(1);

                long totalDaysThisMonth = attendanceRepository
                                .findByStudentIdAndAttendanceDateBetween(studentId, startOfMonth, today).size();
                long presentThisMonth = attendanceRepository.countByStudentIdAndStatusAndDateBetween(studentId,
                                StudentAttendance.AttendanceStatus.PRESENT, startOfMonth, today);

                double percentage = totalDaysThisMonth > 0 ? (double) presentThisMonth / totalDaysThisMonth * 100 : 0.0;

                // Last Month Comparison (Optional but better than hardcoded)
                LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
                LocalDate endOfLastMonth = startOfMonth.minusDays(1);
                long totalDaysLastMonth = attendanceRepository
                                .findByStudentIdAndAttendanceDateBetween(studentId, startOfLastMonth, endOfLastMonth)
                                .size();
                long presentLastMonth = attendanceRepository.countByStudentIdAndStatusAndDateBetween(studentId,
                                StudentAttendance.AttendanceStatus.PRESENT, startOfLastMonth, endOfLastMonth);
                double lastMonthPercentage = totalDaysLastMonth > 0
                                ? (double) presentLastMonth / totalDaysLastMonth * 100
                                : 0.0;

                double diff = percentage - lastMonthPercentage;
                String changePercent = (totalDaysLastMonth > 0)
                                ? String.format("%s%.1f%% from last month", diff >= 0 ? "+" : "", diff)
                                : "No data for last month";

                // Next Class Logic
                TodayScheduleSummary nextClass = getNextClass(currentClassId, currentSectionId);

                // Bus Status Logic
                Map<String, Object> busData = transportRepository.findActiveTripStatus(student.getId(),
                                today.toString());
                StudentDashboardResponse.BusStatusSummary busStatus = null;
                if (busData != null && !busData.isEmpty()) {
                        String tripStatus = String.valueOf(busData.get("trip_status"));
                        String myStopName = String.valueOf(busData.get("my_stop_name"));
                        String myStopTime = String.valueOf(busData.get("my_stop_time"));
                        String nextStopHeading = String.valueOf(busData.getOrDefault("next_stop_heading", "Campus"));

                        String displayStatus = "Scheduled";
                        String displayStop = myStopName;

                        if ("IN_PROGRESS".equals(tripStatus)) {
                                displayStatus = "En Route";
                                displayStop = nextStopHeading != null && !"null".equals(nextStopHeading)
                                                ? "En route to " + nextStopHeading
                                                : "Heading to Campus";
                        }

                        busStatus = StudentDashboardResponse.BusStatusSummary.builder()
                                        .status(displayStatus)
                                        .stopName(displayStop)
                                        .eta(myStopTime)
                                        .build();
                }

                StudentDashboardResponse response = StudentDashboardResponse.builder()
                                .studentName(student.getFirstName()
                                                + (student.getMiddleName() != null ? " " + student.getMiddleName() : "")
                                                + " " + student.getLastName())
                                .admissionNo(student.getAdmissionNo())
                                .profilePicUrl(null)
                                .attendance(StudentDashboardResponse.AttendanceSummary.builder()
                                                .percentage(String.format("%.0f%%", percentage))
                                                .changePercent(changePercent)
                                                .isPositive(diff >= 0)
                                                .build())
                                .todaySchedule(nextClass != null
                                                ? StudentDashboardResponse.TodayScheduleSummary.builder()
                                                                .nextClassSubject(nextClass.getSubjectName())
                                                                .room(nextClass.getRoom())
                                                                .startTime(nextClass.getStartTime())
                                                                .endTime(nextClass.getEndTime())
                                                                .build()
                                                : null)
                                .busStatus(busStatus)
                                .build();

                return StandardResponse.success(response, "Dashboard data fetched successfully");
        }

        public static String getCurrentSession() {
                LocalDate today = LocalDate.now();
                int year = today.getYear();
                int month = today.getMonthValue(); // 1 = Jan, 12 = Dec

                int startYear;
                int endYear;

                if (month >= 4) { // April to December
                        startYear = year;
                        endYear = year + 1;
                } else { // January to March
                        startYear = year - 1;
                        endYear = year;
                }

                // Convert 2025 → 25 format
                String endYearShort = String.valueOf(endYear).substring(2);
                return startYear + "-" + endYearShort;
        }

        private TodayScheduleSummary getNextClass(Integer classId, Integer sectionId) {
                if (classId == null || sectionId == null)
                        return null;

                LocalDate today = LocalDate.now();
                int dayOfWeek = today.getDayOfWeek().getValue(); // 1 = Mon, ..., 7 = Sun
                LocalTime now = LocalTime.now();

                TimeTable tt = timeTableRepository
                                .findByClassIdAndSectionIdAndIsDeletedFalse(classId.longValue(), sectionId.longValue());

                if (tt != null) {
                        List<TimeSlotSubjectMapper> slots = slotMapperRepository.findByTimeTableId(tt.getId());

                        return slots.stream()
                                        .filter(s -> s.getDay() != null && s.getDay() == dayOfWeek)
                                        .filter(s -> {
                                                try {
                                                        return LocalTime.parse(s.getStartTime()).isAfter(now);
                                                } catch (Exception e) {
                                                        return false;
                                                }
                                        })
                                        .min((s1, s2) -> LocalTime.parse(s1.getStartTime())
                                                        .compareTo(LocalTime.parse(s2.getStartTime())))
                                        .map(s -> {
                                                String subjectName = "Unknown";
                                                if (s.getSubjectId() != null) {
                                                        subjectName = subjectRepository
                                                                        .findById(s.getSubjectId().longValue())
                                                                        .map(Subject::getSubjectName).orElse("Unknown");
                                                }
                                                return new TodayScheduleSummary(subjectName, s.getRoom(),
                                                                s.getStartTime(), s.getEndTime());
                                        }).orElse(null);
                }
                return null;
        }

        @Override
        public StandardResponse<?> getAttendanceDetails(Long studentId, LocalDate startDate, LocalDate endDate) {
                List<StudentAttendance> records = attendanceRepository.findByStudentIdAndAttendanceDateBetween(
                                studentId,
                                startDate, endDate);

                long present = records.stream().filter(r -> r.getStatus() == StudentAttendance.AttendanceStatus.PRESENT)
                                .count();
                long absent = records.stream().filter(r -> r.getStatus() == StudentAttendance.AttendanceStatus.ABSENT)
                                .count();
                long holidays = records.stream()
                                .filter(r -> r.getStatus() == StudentAttendance.AttendanceStatus.HOLIDAY).count();

                double percentage = records.size() > 0 ? (double) present / records.size() * 100 : 0.0;

                // Comparison with previous period (same length)
                long daysInRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                LocalDate prevStartDate = startDate.minusDays(daysInRange);
                LocalDate prevEndDate = endDate.minusDays(daysInRange);

                List<StudentAttendance> prevRecords = attendanceRepository
                                .findByStudentIdAndAttendanceDateBetween(studentId, prevStartDate, prevEndDate);
                long prevPresent = prevRecords.stream()
                                .filter(r -> r.getStatus() == StudentAttendance.AttendanceStatus.PRESENT).count();
                double prevPercentage = prevRecords.size() > 0 ? (double) prevPresent / prevRecords.size() * 100 : 0.0;
                double diff = percentage - prevPercentage;

                List<StudentAttendanceDetailResponse.DailyAttendanceDto> calendar = records.stream()
                                .map(r -> StudentAttendanceDetailResponse.DailyAttendanceDto.builder()
                                                .date(r.getAttendanceDate().toString())
                                                .status(r.getStatus().name())
                                                .build())
                                .collect(Collectors.toList());

                StudentAttendanceDetailResponse response = StudentAttendanceDetailResponse.builder()
                                .totalPercentage(String.format("%.1f%%", percentage))
                                .changeVsLastMonth(prevRecords.isEmpty() ? "No prior data"
                                                : String.format("%s%.1f%% vs Previous Period", diff >= 0 ? "+" : "",
                                                                diff))
                                .presentDays((int) present)
                                .absentDays((int) absent)
                                .holidays((int) holidays)
                                .minimumRequiredPercentage("75%")
                                .progressStatus(percentage >= 75 ? "Good Progress" : "Low Attendance")
                                .calendar(calendar)
                                .recentAbsences(records.stream()
                                                .filter(r -> r.getStatus() == StudentAttendance.AttendanceStatus.ABSENT)
                                                .sorted((r1, r2) -> r2.getAttendanceDate()
                                                                .compareTo(r1.getAttendanceDate()))
                                                .limit(5)
                                                .map(r -> StudentAttendanceDetailResponse.RecentAbsenceDto.builder()
                                                                .date(r.getAttendanceDate().toString())
                                                                .reason(r.getRemarks())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();

                return StandardResponse.success(response, "Attendance details fetched successfully");
        }

        @Override
        public StandardResponse<?> getTimetable(Long studentId, Integer dayOfWeek) {
                Student student = studentRepository.findById(studentId.intValue())
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                String sessionText = getCurrentSession();
                Session session = sessionRepository.findBySession(sessionText);
                if (session == null) {
                        session = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("Session " + sessionText
                                                        + " not found and no active session fallback"));
                }

                StudentPromotionMapper promotion = studentPromotionMapperRepository
                                .findActivePromotion(student.getId(), session.getId())
                                .orElse(null);

                Integer classId = (promotion != null) ? promotion.getToClass() : student.getClassApplyingFor();
                Integer sectionId = (promotion != null) ? promotion.getToSection() : student.getSection();

                if (classId == null || sectionId == null) {
                        return StandardResponse.error("Class or Section not assigned to student", "MAPPING_MISSING",
                                        null);
                }

                Optional<TimeTable> tt = timeTableRepository
                                .findAllByFilters(classId.longValue(), sectionId.longValue(), null,
                                                org.springframework.data.domain.Pageable.unpaged())
                                .getContent().stream().findFirst();

                if (tt.isEmpty()) {
                        return StandardResponse.error("No timetable found for this student's class",
                                        "TIMETABLE_NOT_FOUND", null);
                }

                List<TimeSlotSubjectMapper> slots = slotMapperRepository.findByTimeTableId(tt.get().getId())
                                .stream()
                                .filter(s -> s.getDay() != null && s.getDay().equals(dayOfWeek))
                                .sorted((s1, s2) -> {
                                        try {
                                                return LocalTime.parse(s1.getStartTime())
                                                                .compareTo(LocalTime.parse(s2.getStartTime()));
                                        } catch (Exception e) {
                                                return 0;
                                        }
                                })
                                .collect(Collectors.toList());

                List<StudentTimetableDetailResponse.ScheduleSlotDto> schedule = slots.stream()
                                .map(s -> {
                                        String subjectName = "Unknown";
                                        if (s.getSubjectId() != null) {
                                                subjectName = subjectRepository.findById(s.getSubjectId().longValue())
                                                                .map(Subject::getSubjectName).orElse("Unknown");
                                        }
                                        return StudentTimetableDetailResponse.ScheduleSlotDto.builder()
                                                        .subjectName(subjectName)
                                                        .teacherName(s.getTeacherName())
                                                        .room(s.getRoom())
                                                        .startTime(s.getStartTime())
                                                        .endTime(s.getEndTime())
                                                        .isLive(false)
                                                        .status("UPCOMING")
                                                        .build();
                                })
                                .collect(Collectors.toList());

                StudentTimetableDetailResponse response = StudentTimetableDetailResponse.builder()
                                .dayName(java.time.DayOfWeek.of(dayOfWeek).name())
                                .date(LocalDate.now().toString())
                                .schedule(schedule)
                                .build();

                return StandardResponse.success(response, "Timetable fetched successfully");
        }

        @Override
        public StandardResponse<?> getExamResults(Long studentId) {
                List<Marksheet> sheets = marksheetRepository.findByStudentIdAndIsDeletedFalse(studentId);

                if (sheets.isEmpty()) {
                        return StandardResponse.error("No results found for this student", "RESULTS_NOT_FOUND", null);
                }

                String sessionText = getCurrentSession();
                Session tempSession = sessionRepository.findBySession(sessionText);
                if (tempSession == null) {
                        tempSession = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("Session " + sessionText
                                                        + " not found and no active session fallback"));
                }
                final Session currentSession = tempSession;

                // Filter sheets for current session if available, else latest
                Marksheet latest = sheets.stream()
                                .filter(s -> s.getSessionId() != null
                                                && s.getSessionId().equals(currentSession.getId()))
                                .findFirst()
                                .orElse(sheets.get(0));

                List<MarksheetSubjectMarks> subjectMarks = subjectMarksRepository.findByMarksheetId(latest.getId());

                List<StudentExamResultsResponse.SubjectResultDto> results = subjectMarks.stream()
                                .map(sm -> {
                                        String subjectName = "Unknown";
                                        if (sm.getSubjectId() != null) {
                                                subjectName = subjectRepository.findById(sm.getSubjectId().longValue())
                                                                .map(Subject::getSubjectName).orElse("Unknown");
                                        }
                                        return StudentExamResultsResponse.SubjectResultDto.builder()
                                                        .subjectName(subjectName)
                                                        .marksObtained(sm.getTotalMarks())
                                                        .maxMarks(sm.getTotalMax())
                                                        .grade(calculateGrade(sm.getTotalMarks(), sm.getTotalMax()))
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Rank Calculation
                String rankText = "N/A";
                if (latest.getClassId() != null && latest.getSessionId() != null) {
                        List<Marksheet> classSheets = marksheetRepository
                                        .findByClassIdAndSessionIdAndIsDeletedFalseOrderByPercentageDesc(
                                                        latest.getClassId(), latest.getSessionId());
                        int rank = 1;
                        for (Marksheet s : classSheets) {
                                if (s.getStudentId().equals(studentId))
                                        break;
                                rank++;
                        }
                        rankText = String.format("%d%s of %d students", rank, getOrdinal(rank), classSheets.size());
                }

                String termName = Optional.ofNullable(latest.getExamTypeId())
                                .flatMap(commonMasterRepository::findById)
                                .map(CommonMaster::getData)
                                .orElse("Main Results");

                StudentExamResultsResponse response = StudentExamResultsResponse.builder()
                                .academicYear(currentSession.getSession())
                                .results(results)
                                .summary(StudentExamResultsResponse.OverallPerformanceDto.builder()
                                                .termName(termName)
                                                .totalPercentage(String.format("%.1f%%", latest.getPercentage()))
                                                .resultStatus(latest.getPercentage() >= 33 ? "Passed" : "Failed")
                                                .classRank(rankText)
                                                .build())
                                .build();

                return StandardResponse.success(response, "Exam results fetched successfully");
        }

        private String getOrdinal(int i) {
                String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
                switch (i % 100) {
                        case 11:
                        case 12:
                        case 13:
                                return "th";
                        default:
                                return suffixes[i % 10];
                }
        }

        @Override
        public StandardResponse<?> getFeesDetails(Long studentId) {
                Student student = studentRepository.findById(studentId.intValue())
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                String sessionText = getCurrentSession();
                Session session = sessionRepository.findBySession(sessionText);
                if (session == null) {
                        session = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("Session " + sessionText
                                                        + " not found and no active session fallback"));
                }

                // Fetch Fee Structure
                Map<String, Object> feeStructure = feeMobileRepository.findFeeStructureByClassAndAcademicYear(
                                student.getClassApplyingFor(), session.getId());

                List<StudentFeesResponse.FeeItemDto> feeItems = new ArrayList<>();
                Double totalFees = 0.0;

                if (feeStructure != null) {
                        String[] feeTypes = { "tuition_fee", "admission_fee", "transport_fee", "library_fee",
                                        "exam_fee", "sports_fee", "lab_fee", "development_fee" };
                        String[] displayNames = { "Tuition Fee", "Admission Fee", "Conveyance Fee", "Library Fee",
                                        "Exam Fee", "Sports Fee", "Lab Fee", "Development Fee" };

                        for (int i = 0; i < feeTypes.length; i++) {
                                Object val = feeStructure.get(feeTypes[i]);
                                if (val != null) {
                                        Double amount = Double.valueOf(val.toString());
                                        if (amount > 0) {
                                                feeItems.add(StudentFeesResponse.FeeItemDto.builder()
                                                                .feeType(displayNames[i])
                                                                .amount(amount)
                                                                .build());
                                        }
                                }
                        }
                        totalFees = feeStructure.get("total_fee") != null
                                        ? Double.valueOf(feeStructure.get("total_fee").toString())
                                        : 0.0;
                }

                List<Map<String, Object>> payments = feeMobileRepository.findPaymentsByStudentId(studentId.intValue());
                List<StudentFeesResponse.PaymentHistoryDto> history = new ArrayList<>();
                Double paidAmount = 0.0;

                if (payments != null) {
                        for (Map<String, Object> p : payments) {
                                Double netAmount = p.get("net_amount") != null
                                                ? Double.valueOf(p.get("net_amount").toString())
                                                : 0.0;
                                paidAmount += netAmount;

                                history.add(StudentFeesResponse.PaymentHistoryDto.builder()
                                                .id(Long.valueOf(p.get("id").toString()))
                                                .title("Fee Payment")
                                                .date(p.get("paid_at") != null
                                                                ? p.get("paid_at").toString().split("T")[0]
                                                                : "")
                                                .paymentMethod(p.get("payment_method") != null
                                                                ? p.get("payment_method").toString()
                                                                : "UNKNOWN")
                                                .amount(netAmount)
                                                .status("SUCCESS")
                                                .receiptUrl(null)
                                                .build());
                        }
                }

                StudentFeesResponse response = StudentFeesResponse.builder()
                                .academicYear(session.getSession())
                                .totalFees(totalFees)
                                .paidAmount(paidAmount)
                                .remainingAmount(Math.max(0, totalFees - paidAmount))
                                .feeStructure(feeItems)
                                .paymentHistory(history)
                                .build();

                return StandardResponse.success(response, "Fees details fetched successfully");
        }

        private String calculateGrade(int obtained, int max) {
                if (max <= 0)
                        return "N/A";
                double p = (double) obtained / max * 100;
                if (p >= 90)
                        return "A1";
                if (p >= 80)
                        return "A2";
                if (p >= 70)
                        return "B1";
                if (p >= 60)
                        return "B2";
                if (p >= 50)
                        return "C";
                if (p >= 33)
                        return "D";
                return "E";
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        private static class TodayScheduleSummary {
                private String subjectName;
                private String room;
                private String startTime;
                private String endTime;
        }
}
