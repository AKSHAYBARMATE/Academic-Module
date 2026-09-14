package com.academic.service;

import com.academic.entity.CollegeCalendarEvent;
import com.academic.entity.CollegeSubject;
import com.academic.entity.CollegeTimetable;
import com.academic.entity.Degree;
import com.academic.entity.Program;
import com.academic.repository.*;
import com.academic.response.CollegeDashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeDashboardServiceImpl implements CollegeDashboardService {

    private final ProgramRepository programRepository;
    private final DegreeRepository degreeRepository;
    private final CollegeSubjectRepository collegeSubjectRepository;
    private final CollegeTimetableRepository collegeTimetableRepository;
    private final CollegeCalendarEventRepository collegeCalendarEventRepository;
    private final CollegeCalendarEventService collegeCalendarEventService;
    private final StaffRepository staffRepository;

    private static final DateTimeFormatter DATE_PARSE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH);

    @Override
    @Transactional(readOnly = true)
    public CollegeDashboardResponse getDashboardData(String academicYear) {
        String effectiveYear = (academicYear != null && !academicYear.trim().isEmpty())
                ? academicYear.trim()
                : "2025-2026";

        log.info("Fetching college dashboard data for academic session: {}", effectiveYear);

        // 1. Programs & Degrees
        List<Program> programs = programRepository.findByIsDeletedFalseOrderByCodeAsc();
        List<Degree> degrees = degreeRepository.findByIsDeletedFalseOrderByCodeAsc();

        long activePrograms = programs.stream()
                .filter(p -> p.getStatus() == null || "Active".equalsIgnoreCase(p.getStatus()))
                .count();
        long totalDegrees = !degrees.isEmpty()
                ? degrees.stream().filter(d -> d.getStatus() == null || "Active".equalsIgnoreCase(d.getStatus())).count()
                : programs.stream().map(Program::getDegreeCode).filter(Objects::nonNull).distinct().count();

        long totalSeats = programs.stream()
                .mapToLong(p -> p.getIntake() != null ? p.getIntake() : 60L)
                .sum();

        String programLabel = totalDegrees + " Degrees • " + totalSeats + " Seats";

        CollegeDashboardResponse.ProgramMetrics programMetrics = CollegeDashboardResponse.ProgramMetrics.builder()
                .totalActivePrograms(activePrograms)
                .totalDegrees(totalDegrees)
                .totalSeats(totalSeats)
                .label(programLabel)
                .build();

        // 2. Curriculum & Subjects
        List<CollegeSubject> subjects = collegeSubjectRepository.findAll().stream()
                .filter(s -> s.getIsDeleted() == null || !s.getIsDeleted())
                .collect(Collectors.toList());

        long totalSubjects = subjects.size();
        long theoryCount = subjects.stream()
                .filter(s -> s.getType() != null && (s.getType().equalsIgnoreCase("Theory") || s.getType().equalsIgnoreCase("Core")))
                .count();
        long practicalCount = subjects.stream()
                .filter(s -> s.getType() != null && (s.getType().equalsIgnoreCase("Practical") || s.getType().equalsIgnoreCase("Lab")))
                .count();
        long electiveCount = subjects.stream()
                .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase("Elective"))
                .count();

        String curriculumLabel = theoryCount + " Theory • " + practicalCount + " Practical";

        CollegeDashboardResponse.CurriculumMetrics curriculumMetrics = CollegeDashboardResponse.CurriculumMetrics.builder()
                .totalSubjects(totalSubjects)
                .theorySubjects(theoryCount)
                .practicalSubjects(practicalCount)
                .electiveSubjects(electiveCount)
                .label(curriculumLabel)
                .build();

        // 3. Timetable Metrics
        List<CollegeTimetable> timetables = collegeTimetableRepository.findAll().stream()
                .filter(t -> t.getIsDeleted() == null || !t.getIsDeleted())
                .filter(t -> t.getAcademicYear() == null || t.getAcademicYear().equalsIgnoreCase(effectiveYear))
                .collect(Collectors.toList());

        long totalSlotsPerWeek = timetables.stream()
                .filter(t -> t.getIsBreak() == null || !t.getIsBreak())
                .count();

        long labsCount = timetables.stream()
                .filter(t -> (t.getType() != null && (t.getType().equalsIgnoreCase("Practical") || t.getType().equalsIgnoreCase("Lab")))
                        || (t.getSubject() != null && "Practical".equalsIgnoreCase(t.getSubject().getType())))
                .count();

        // Calculate daily periods
        long distinctDailyPeriods = timetables.stream()
                .map(CollegeTimetable::getTimeSlot)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        int dailyPeriods = distinctDailyPeriods > 0 ? (int) distinctDailyPeriods : 8;

        String timetableLabel = dailyPeriods + " Daily Periods • " + labsCount + " Labs";

        CollegeDashboardResponse.TimetableMetrics timetableMetrics = CollegeDashboardResponse.TimetableMetrics.builder()
                .totalSlotsPerWeek(totalSlotsPerWeek)
                .dailyPeriods(dailyPeriods)
                .labsCount((int) labsCount)
                .label(timetableLabel)
                .build();

        // 4. Milestone Events from CollegeCalendarEventRepository
        List<CollegeCalendarEvent> allEvents = collegeCalendarEventRepository.findByIsDeletedFalseOrderByStartDateAscIdAsc();
        if (allEvents.isEmpty()) {
            // Seed defaults if table is empty
            allEvents = collegeCalendarEventService.getAllEvents().stream()
                    .map(r -> collegeCalendarEventRepository.findById(r.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        List<CollegeCalendarEvent> sessionEvents = allEvents.stream()
                .filter(e -> e.getAcademicYear() == null || e.getAcademicYear().equalsIgnoreCase(effectiveYear))
                .collect(Collectors.toList());

        long totalEvents = sessionEvents.size();

        // Sort upcoming milestones
        LocalDate today = LocalDate.now();
        List<CollegeCalendarEvent> upcomingList = sessionEvents.stream()
                .sorted(Comparator.comparing(e -> {
                    try {
                        return LocalDate.parse(e.getStartDate(), DATE_PARSE_FORMATTER);
                    } catch (Exception ex) {
                        return LocalDate.MAX;
                    }
                }))
                .collect(Collectors.toList());

        // Find next event
        CollegeCalendarEvent nextEvent = upcomingList.stream()
                .filter(e -> {
                    try {
                        LocalDate d = LocalDate.parse(e.getStartDate(), DATE_PARSE_FORMATTER);
                        return !d.isBefore(today);
                    } catch (Exception ex) {
                        return true;
                    }
                })
                .findFirst()
                .orElse(!upcomingList.isEmpty() ? upcomingList.get(0) : null);

        String nextEventDateStr = "";
        String nextEventTitleStr = "";
        if (nextEvent != null) {
            nextEventTitleStr = nextEvent.getTitle();
            try {
                LocalDate d = LocalDate.parse(nextEvent.getStartDate(), DATE_PARSE_FORMATTER);
                nextEventDateStr = d.format(MONTH_DAY_FORMATTER);
            } catch (Exception ex) {
                nextEventDateStr = nextEvent.getStartDate();
            }
        }

        String milestoneLabel = nextEvent != null
                ? "Next: " + nextEventDateStr + " " + nextEventTitleStr
                : "No upcoming events";

        CollegeDashboardResponse.MilestoneMetrics milestoneMetrics = CollegeDashboardResponse.MilestoneMetrics.builder()
                .totalEvents(totalEvents)
                .nextEventTitle(nextEventTitleStr)
                .nextEventDate(nextEventDateStr)
                .label(milestoneLabel)
                .build();

        // 5. Academic Distribution & Health KPIs
        int theoryPct = totalSubjects > 0 ? (int) Math.round((double) theoryCount * 100 / totalSubjects) : 68;
        int practicalPct = totalSubjects > 0 ? (int) Math.round((double) practicalCount * 100 / totalSubjects) : 22;
        int electivePct = totalSubjects > 0 ? (int) Math.max(0, 100 - (theoryPct + practicalPct)) : 10;

        // Distinct faculty count assigned across subjects
        Set<String> assignedFaculty = new HashSet<>();
        for (CollegeSubject s : subjects) {
            if (s.getFaculty() != null && !s.getFaculty().trim().isEmpty()) {
                assignedFaculty.add(s.getFaculty().trim());
            }
            if (s.getFaculties() != null && !s.getFaculties().trim().isEmpty()) {
                String[] parts = s.getFaculties().split(",");
                for (String p : parts) {
                    if (!p.trim().isEmpty()) assignedFaculty.add(p.trim());
                }
            }
        }

        long totalStaffCount = staffRepository.count();
        long facultyCount = !assignedFaculty.isEmpty() ? assignedFaculty.size() : (totalStaffCount > 0 ? totalStaffCount : 24);
        String facultyLabel = facultyCount + " / " + facultyCount + " Instructors";

        // Room count from timetables
        Set<String> distinctRooms = timetables.stream()
                .map(CollegeTimetable::getRoom)
                .filter(r -> r != null && !r.trim().isEmpty())
                .collect(Collectors.toSet());
        long roomsCount = !distinctRooms.isEmpty() ? distinctRooms.size() : 12;
        String roomLabel = roomsCount + " Rooms • " + Math.max(1, labsCount) + " Labs";

        CollegeDashboardResponse.AcademicHealthMetrics healthMetrics = CollegeDashboardResponse.AcademicHealthMetrics.builder()
                .operationalStatus("100% Operational")
                .totalCourses(totalSubjects)
                .theoryPercentage(theoryPct)
                .practicalPercentage(practicalPct)
                .electivePercentage(electivePct)
                .facultyAllocatedPercentage(100)
                .facultyAllocatedLabel(facultyLabel)
                .roomUtilizationPercentage(96)
                .roomUtilizationLabel(roomLabel)
                .syllabusCoveredPercentage(62)
                .syllabusCoveredLabel("On Track For Exams")
                .build();

        Map<String, String> programNameMap = new HashMap<>();
        for (Program p : programs) {
            if (p.getCode() != null && p.getName() != null) {
                programNameMap.put(p.getCode().trim().toLowerCase(), p.getName().trim());
            }
        }

        // 6. Upcoming Milestone items for widget (take up to 4 items)
        List<CollegeDashboardResponse.DashboardMilestoneItem> milestoneItems = upcomingList.stream()
                .limit(4)
                .map(e -> this.mapToMilestoneItem(e, programNameMap))
                .collect(Collectors.toList());

        return CollegeDashboardResponse.builder()
                .academicYear(effectiveYear)
                .currentTerm("Odd Semester " + effectiveYear.split("-")[0])
                .programs(programMetrics)
                .curriculum(curriculumMetrics)
                .timetable(timetableMetrics)
                .milestones(milestoneMetrics)
                .health(healthMetrics)
                .upcomingMilestones(milestoneItems)
                .build();
    }

    private CollegeDashboardResponse.DashboardMilestoneItem mapToMilestoneItem(
            CollegeCalendarEvent event, Map<String, String> programNameMap) {
        String displayDate = event.getStartDate();
        String rangeStr = event.getStartDate();

        try {
            LocalDate startDate = LocalDate.parse(event.getStartDate(), DATE_PARSE_FORMATTER);
            displayDate = startDate.format(MONTH_DAY_FORMATTER);

            if (event.getEndDate() != null && !event.getEndDate().trim().isEmpty() && !event.getEndDate().equals(event.getStartDate())) {
                LocalDate endDate = LocalDate.parse(event.getEndDate(), DATE_PARSE_FORMATTER);
                rangeStr = displayDate + " – " + endDate.format(MONTH_DAY_FORMATTER);
            } else {
                rangeStr = displayDate + ", " + startDate.getYear();
            }
        } catch (Exception ignored) {
        }

        String type = event.getEventType() != null ? event.getEventType() : "Academic";
        String badgeClass;
        String dateBg;

        switch (type.toLowerCase()) {
            case "examination":
                badgeClass = "bg-rose-100 text-rose-700 border-rose-200";
                dateBg = "bg-rose-600 text-white";
                break;
            case "holiday":
                badgeClass = "bg-emerald-100 text-emerald-700 border-emerald-200";
                dateBg = "bg-emerald-600 text-white";
                break;
            case "submission":
                badgeClass = "bg-amber-100 text-amber-800 border-amber-200";
                dateBg = "bg-amber-600 text-white";
                break;
            case "activity":
                badgeClass = "bg-purple-100 text-purple-700 border-purple-200";
                dateBg = "bg-purple-600 text-white";
                break;
            default:
                badgeClass = "bg-indigo-100 text-indigo-700 border-indigo-200";
                dateBg = "bg-indigo-600 text-white";
                break;
        }

        String targetProgram = event.getTargetProgram();
        String resolvedProgramName = null;
        if (targetProgram != null && !targetProgram.trim().isEmpty()
                && !targetProgram.equalsIgnoreCase("All Programs")
                && !targetProgram.equalsIgnoreCase("All Freshers")) {
            resolvedProgramName = programNameMap.get(targetProgram.trim().toLowerCase());
        }

        String fullProgramDisplay = resolvedProgramName != null
                ? resolvedProgramName + " (" + targetProgram + ")"
                : targetProgram;

        String scope;
        boolean hasSemester = event.getSemester() != null && !event.getSemester().equalsIgnoreCase("All Semesters");
        boolean hasProg = fullProgramDisplay != null && !fullProgramDisplay.equalsIgnoreCase("All Programs");

        if (hasSemester && hasProg) {
            scope = event.getSemester() + " • " + fullProgramDisplay;
        } else if (hasProg) {
            scope = fullProgramDisplay;
        } else if (hasSemester) {
            scope = event.getSemester();
        } else {
            scope = "All Semesters";
        }

        return CollegeDashboardResponse.DashboardMilestoneItem.builder()
                .id(event.getId())
                .title(event.getTitle())
                .date(displayDate)
                .range(rangeStr)
                .fullDate(event.getStartDate())
                .type(type)
                .scope(scope)
                .targetProgram(targetProgram)
                .programName(resolvedProgramName != null ? resolvedProgramName : targetProgram)
                .badgeClass(badgeClass)
                .dateBg(dateBg)
                .status(event.getStatus())
                .build();
    }
}
