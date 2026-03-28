package com.academic.service;

import com.academic.dto.mobile.*;
import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.response.StandardResponse;
import com.academic.utility.IstClock;
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
        // private final MarksheetSubjectMarksRepository subjectMarksRepository;
        private final SessionRepository sessionRepository;
        private final FeeStructureRepository feeStructureRepository;
        private final FeePaymentRepository feePaymentRepository;
        private final CommonMasterRepository commonMasterRepository;
        private final TransportRepository transportRepository;
        private final ExamScheduleRepository examScheduleRepository;
        private final ExamSubjectConfigRepository examSubjectConfigRepository;
        private final AcademicCalendarEventRepository academicCalendarEventRepository;
        private final ExamSetupRepository examSetupRepository;

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
                LocalDate today = IstClock.today();
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
                LocalDate today = IstClock.today();
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

                LocalDate today = IstClock.today();
                int dayOfWeek = today.getDayOfWeek().getValue(); // 1 = Mon, ..., 7 = Sun
                LocalTime now = IstClock.nowTime();

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

                TimeTable tt = timeTableRepository
                                .findByClassIdAndSectionIdAndIsDeletedFalse(classId.longValue(), sectionId.longValue());

                if (tt == null) {
                        return StandardResponse.error("No timetable found for this student's class",
                                        "TIMETABLE_NOT_FOUND", null);
                }

                List<TimeSlotSubjectMapper> slots = slotMapperRepository.findByTimeTableId(tt.getId())
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

                // Determine if the requested day is today (for live/completed logic)
                boolean isToday = IstClock.today().getDayOfWeek().getValue() == dayOfWeek;
                LocalTime nowTime = IstClock.nowTime();

                List<StudentTimetableDetailResponse.ScheduleSlotDto> schedule = slots.stream()
                                .map(s -> {
                                        String subjectName = "Unknown";
                                        if (s.getSubjectId() != null) {
                                                subjectName = subjectRepository.findById(s.getSubjectId().longValue())
                                                                .map(Subject::getSubjectName).orElse("Unknown");
                                        }

                                        // Compute dynamic status only when viewing today's timetable
                                        boolean live = false;
                                        String status = "UPCOMING";

                                        if (isToday && s.getStartTime() != null && s.getEndTime() != null) {
                                                try {
                                                        LocalTime slotStart = LocalTime.parse(s.getStartTime());
                                                        LocalTime slotEnd = LocalTime.parse(s.getEndTime());

                                                        if (nowTime.isAfter(slotEnd)) {
                                                                // Class has ended
                                                                status = "COMPLETED";
                                                        } else if (!nowTime.isBefore(slotStart)
                                                                        && !nowTime.isAfter(slotEnd)) {
                                                                // now >= slotStart AND now <= slotEnd → class is
                                                                // running
                                                                status = "LIVE";
                                                                live = true;
                                                        }
                                                        // else nowTime < slotStart → UPCOMING (default)
                                                } catch (Exception ignored) {
                                                        // Malformed time string — leave as UPCOMING
                                                }
                                        }

                                        String startTimeFormatted = s.getStartTime();
                                        String endTimeFormatted = s.getEndTime();

                                        try {
                                                java.time.format.DateTimeFormatter outputFormat = java.time.format.DateTimeFormatter
                                                                .ofPattern("hh:mm a");

                                                if (s.getStartTime() != null) {
                                                        startTimeFormatted = LocalTime.parse(s.getStartTime())
                                                                        .format(outputFormat);
                                                }
                                                if (s.getEndTime() != null) {
                                                        endTimeFormatted = LocalTime.parse(s.getEndTime())
                                                                        .format(outputFormat);
                                                }
                                        } catch (Exception ignored) {
                                        }

                                        return StudentTimetableDetailResponse.ScheduleSlotDto.builder()
                                                        .subjectName(subjectName)
                                                        .teacherName(s.getTeacherName())
                                                        .room(s.getRoom())
                                                        .startTime(startTimeFormatted)
                                                        .endTime(endTimeFormatted)
                                                        .isLive(live)
                                                        .status(status)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                StudentTimetableDetailResponse response = StudentTimetableDetailResponse.builder()
                                .dayName(java.time.DayOfWeek.of(dayOfWeek).name())
                                .date(IstClock.today().toString())
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

                List<StudentExamResultsResponse.SubjectResultDto> results = latest.getSubjects().stream()
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
                                if (s.getStudentId().equals(studentId.intValue()))
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

                // ── 1. Resolve current session ──────────────────────────────────────────
                String sessionText = getCurrentSession();
                Session session = sessionRepository.findBySession(sessionText);
                if (session == null) {
                        session = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("Session " + sessionText
                                                        + " not found and no active session fallback"));
                }
                final String academicYearLabel = session.getSession(); // e.g. "2025-26"

                // ── 2. Resolve student's current class ──────────────────────────────────
                StudentPromotionMapper promotion = studentPromotionMapperRepository
                                .findActivePromotion(student.getId(), session.getId())
                                .orElse(null);
                Integer classId = (promotion != null) ? promotion.getToClass() : student.getClassApplyingFor();

                // ── 3. Resolve Fee Structure via FeeStructure entity ─────────────────────
                // FeeStructure.academicYear is a CommonMaster row whose 'data' = session string
                List<StudentFeesResponse.FeeItemDto> feeItems = new ArrayList<>();
                double totalFees = 0.0;

                if (classId != null) {
                        // Try to find academic year CommonMaster by session text
                        Optional<CommonMaster> academicYearCm = commonMasterRepository
                                        .findByDataAndStatusTrue(academicYearLabel);

                        Optional<FeeStructure> feeStructureOpt = academicYearCm.isPresent()
                                        ? feeStructureRepository.findByClassIdAndAcademicYearId(
                                                        classId.longValue(),
                                                        academicYearCm.get().getId().longValue())
                                        : Optional.empty();

                        // Fallback: latest active fee structure for this class
                        if (feeStructureOpt.isEmpty()) {
                                List<FeeStructure> fallbacks = feeStructureRepository
                                                .findByClassIdOrderByEffectiveFromDesc(classId.longValue());
                                feeStructureOpt = fallbacks.isEmpty() ? Optional.empty()
                                                : Optional.of(fallbacks.get(0));
                        }

                        if (feeStructureOpt.isPresent()) {
                                FeeStructure fs = feeStructureOpt.get();

                                addFeeItem(feeItems, "Tuition Fee", fs.getTuitionFee());
                                addFeeItem(feeItems, "Admission Fee", fs.getAdmissionFee());
                                addFeeItem(feeItems, "Transport Fee", fs.getTransportFee());
                                addFeeItem(feeItems, "Library Fee", fs.getLibraryFee());
                                addFeeItem(feeItems, "Exam Fee", fs.getExamFee());
                                addFeeItem(feeItems, "Sports Fee", fs.getSportsFee());
                                addFeeItem(feeItems, "Lab Fee", fs.getLabFee());
                                addFeeItem(feeItems, "Development Fee", fs.getDevelopmentFee());

                                totalFees = fs.getTotalFee() != null
                                                ? fs.getTotalFee().doubleValue()
                                                : feeItems.stream()
                                                                .mapToDouble(StudentFeesResponse.FeeItemDto::getAmount)
                                                                .sum();
                        }
                }

                // ── 4. Fetch payment history from FeePayment entity ──────────────────────
                List<FeePayment> payments = feePaymentRepository.findByStudentId(studentId.intValue());
                List<StudentFeesResponse.PaymentHistoryDto> history = new ArrayList<>();
                double paidAmount = 0.0;

                for (FeePayment p : payments) {
                        double net = p.getNetAmount() != null ? p.getNetAmount().doubleValue() : 0.0;
                        paidAmount += net;

                        // Build a descriptive title from fee categories (if present)
                        String title = "Fee Payment";
                        if (p.getFeeStructure() != null && p.getFeeStructure().getFeeStructureName() != null) {
                                title = p.getFeeStructure().getFeeStructureName();
                        }

                        history.add(StudentFeesResponse.PaymentHistoryDto.builder()
                                        .id(p.getId())
                                        .title(title)
                                        .date(p.getPaidAt() != null
                                                        ? p.getPaidAt().toLocalDate().toString()
                                                        : (p.getDueDate() != null ? p.getDueDate().toString() : ""))
                                        .paymentMethod(p.getPaymentMethod() != null
                                                        ? p.getPaymentMethod()
                                                        : "UNKNOWN")
                                        .amount(net)
                                        .status("SUCCESS")
                                        .receiptUrl(p.getReceiptNo())
                                        .build());
                }

                StudentFeesResponse response = StudentFeesResponse.builder()
                                .academicYear(academicYearLabel)
                                .totalFees(totalFees)
                                .paidAmount(paidAmount)
                                .remainingAmount(Math.max(0, totalFees - paidAmount))
                                .feeStructure(feeItems)
                                .paymentHistory(history)
                                .build();

                return StandardResponse.success(response, "Fees details fetched successfully");
        }

        /** Helper: add a fee item only if the amount is non-null and > 0. */
        private void addFeeItem(List<StudentFeesResponse.FeeItemDto> items, String label, java.math.BigDecimal value) {
                if (value != null && value.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        items.add(StudentFeesResponse.FeeItemDto.builder()
                                        .feeType(label)
                                        .amount(value.doubleValue())
                                        .build());
                }
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

        // ══════════════════════════════════════════════════════════════════
        // EXAM SCHEDULE
        // ══════════════════════════════════════════════════════════════════
        @Override
        public StandardResponse<?> getExamSchedule(Long studentId) {
                Student student = studentRepository.findById(studentId.intValue())
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                // Resolve current session
                String sessionText = getCurrentSession();
                Session tempSession = sessionRepository.findBySession(sessionText);
                if (tempSession == null) {
                        tempSession = sessionRepository.findByIsActiveTrue()
                                        .orElseThrow(() -> new RuntimeException("No active session found"));
                }
                final Session session = tempSession;

                // Resolve student's class
                StudentPromotionMapper promotion = studentPromotionMapperRepository
                                .findActivePromotion(student.getId(), session.getId())
                                .orElse(null);
                Integer classId = (promotion != null) ? promotion.getToClass() : student.getClassApplyingFor();

                if (classId == null) {
                        return StandardResponse.error("Class not assigned to student", "CLASS_MISSING", null);
                }

                // Fetch all active exam schedules for current session
                List<ExamSchedule> schedules = examScheduleRepository
                                .findBySession_IdAndIsActiveTrue(session.getId());

                if (schedules.isEmpty()) {
                        return StandardResponse.error("No exam schedules found for current session",
                                        "SCHEDULE_NOT_FOUND", null);
                }

                // Use the latest/first published schedule (prefer PUBLISHED over DRAFT)
                ExamSchedule schedule = schedules.stream()
                                .filter(s -> "PUBLISHED".equalsIgnoreCase(s.getStatus()))
                                .findFirst()
                                .orElse(schedules.get(0));

                // Fetch Exam Setup for this session and class
                List<ExamSetup> examSetups = examSetupRepository
                                .findByAcademicYearIdAndClassIdAndIsDeletedFalseOrderByExamDateAsc(
                                                session.getId(), classId);

                List<StudentExamScheduleResponse.SubjectExamDto> subjects = examSetups.stream()
                                .map(es -> {
                                        // Find corresponding config for marks distribution
                                        Optional<ExamSubjectConfig> config = examSubjectConfigRepository
                                                        .findBySession_IdAndExamType_IdAndClassId_IdAndSubject_IdAndIsDeleteFalse(
                                                                        session.getId(),
                                                                        schedule.getExamType().getId(),
                                                                        classId,
                                                                        Long.valueOf(es.getSubjectId()));

                                        List<StudentExamScheduleResponse.ComponentDto> comps = new ArrayList<>();
                                        int total = es.getMaxMarks() != null ? es.getMaxMarks() : 0;

                                        if (config.isPresent()) {
                                                comps = config.get().getComponents().stream()
                                                                .map(c -> StudentExamScheduleResponse.ComponentDto
                                                                                .builder()
                                                                                .componentName(c.getComponent()
                                                                                                .getComponentName())
                                                                                .maxMarks(c.getMaxMarks())
                                                                                .build())
                                                                .collect(Collectors.toList());
                                                total = comps.stream().mapToInt(
                                                                c -> c.getMaxMarks() != null ? c.getMaxMarks() : 0)
                                                                .sum();
                                        }

                                        String subjectName = subjectRepository.findById(Long.valueOf(es.getSubjectId()))
                                                        .map(Subject::getSubjectName).orElse("Unknown");

                                        return StudentExamScheduleResponse.SubjectExamDto.builder()
                                                        .subjectName(subjectName)
                                                        .examDate(es.getExamDate() != null
                                                                        ? es.getExamDate().format(
                                                                                        java.time.format.DateTimeFormatter
                                                                                                        .ofPattern("MMM dd, EEEE"))
                                                                        : "TBD")
                                                        .components(comps)
                                                        .totalMarks(total)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                StudentExamScheduleResponse response = StudentExamScheduleResponse.builder()
                                .examTitle(schedule.getExamTitle())
                                .examType(schedule.getExamType() != null
                                                ? schedule.getExamType().getData()
                                                : "")
                                .academicYear(session.getSession())
                                .startDate(schedule.getStartDate() != null
                                                ? schedule.getStartDate().toString()
                                                : null)
                                .endDate(schedule.getEndDate() != null
                                                ? schedule.getEndDate().toString()
                                                : null)
                                .status(schedule.getStatus())
                                .subjects(subjects)
                                .build();

                return StandardResponse.success(response, "Exam schedule fetched successfully");
        }

        // ══════════════════════════════════════════════════════════════════
        // ACADEMIC CALENDAR
        // ══════════════════════════════════════════════════════════════════
        @Override
        public StandardResponse<?> getAcademicCalendar(Long studentId) {
                Student student = studentRepository.findById(studentId.intValue())
                                .orElseThrow(() -> new RuntimeException("Student not found"));

                // Resolve student's class for filtering
                String sessionText = getCurrentSession();
                Session session = sessionRepository.findBySession(sessionText);
                if (session == null) {
                        session = sessionRepository.findByIsActiveTrue().orElse(null);
                }

                StudentPromotionMapper promotion = (session != null)
                                ? studentPromotionMapperRepository
                                                .findActivePromotion(student.getId(), session.getId())
                                                .orElse(null)
                                : null;
                Integer classId = (promotion != null) ? promotion.getToClass() : student.getClassApplyingFor();

                // Fetch events — filter by class if we know the class, else return all
                List<AcademicCalendarEvent> events;
                if (classId != null) {
                        // JSON_CONTAINS query — classId passed as JSON number string e.g. "5"
                        events = academicCalendarEventRepository.findEventsForClass(String.valueOf(classId));
                } else {
                        events = academicCalendarEventRepository.findAllByIsDeletedFalse();
                }

                LocalDate today = IstClock.today();

                List<AcademicCalendarResponse.CalendarEventDto> dtos = events.stream()
                                .map(e -> {
                                        // Compute status: COMPLETED if date < today, UPCOMING otherwise
                                        String status = (e.getStatus() != null && !e.getStatus().isBlank())
                                                        ? e.getStatus()
                                                        : (e.getDate() != null && e.getDate().isBefore(today)
                                                                        ? "COMPLETED"
                                                                        : "UPCOMING");

                                        return AcademicCalendarResponse.CalendarEventDto.builder()
                                                        .id(e.getId())
                                                        .eventName(e.getEventName())
                                                        .date(e.getDate() != null ? e.getDate().toString() : null)
                                                        .type(e.getType())
                                                        .duration(e.getDuration())
                                                        .status(status)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                AcademicCalendarResponse response = AcademicCalendarResponse.builder()
                                .events(dtos)
                                .totalEvents(dtos.size())
                                .build();

                return StandardResponse.success(response, "Academic calendar fetched successfully");
        }
}
