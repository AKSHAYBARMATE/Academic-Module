package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeDashboardResponse {

    private String academicYear;
    private String currentTerm;

    // 1. Program Metrics
    private ProgramMetrics programs;

    // 2. Curriculum Metrics
    private CurriculumMetrics curriculum;

    // 3. Timetable Metrics
    private TimetableMetrics timetable;

    // 4. Milestone Metrics
    private MilestoneMetrics milestones;

    // 5. Academic Distribution / Health KPIs
    private AcademicHealthMetrics health;

    // 6. Upcoming Milestones List
    private List<DashboardMilestoneItem> upcomingMilestones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramMetrics {
        private long totalActivePrograms;
        private long totalDegrees;
        private long totalSeats;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurriculumMetrics {
        private long totalSubjects;
        private long theorySubjects;
        private long practicalSubjects;
        private long electiveSubjects;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimetableMetrics {
        private long totalSlotsPerWeek;
        private int dailyPeriods;
        private int labsCount;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneMetrics {
        private long totalEvents;
        private String nextEventTitle;
        private String nextEventDate;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicHealthMetrics {
        private String operationalStatus;
        private long totalCourses;
        private int theoryPercentage;
        private int practicalPercentage;
        private int electivePercentage;
        private int facultyAllocatedPercentage;
        private String facultyAllocatedLabel;
        private int roomUtilizationPercentage;
        private String roomUtilizationLabel;
        private int syllabusCoveredPercentage;
        private String syllabusCoveredLabel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardMilestoneItem {
        private Long id;
        private String title;
        private String date;       // e.g. "Oct 20"
        private String range;      // e.g. "Oct 20 – Oct 25"
        private String fullDate;   // e.g. "2025-10-20"
        private String type;       // Examination, Holiday, Submission, Academic, Activity
        private String scope;      // e.g. "Semester 3" or targetProgram
        private String badgeClass; // css class or category styling
        private String dateBg;     // css background styling
        private String status;     // Scheduled, Active, Completed
    }
}
