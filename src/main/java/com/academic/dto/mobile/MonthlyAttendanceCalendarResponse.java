package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlyAttendanceCalendarResponse {

    private String classSectionName;

    /** e.g. "JULY 2026" */
    private String monthLabel;

    /** 1–12 */
    private int month;

    /** e.g. 2026 */
    private int year;

    /** One entry per calendar day of the requested month */
    private List<DailyRecord> dailyRecords;

    /** Aggregated summary for the entire month */
    private MonthlySummary monthlySummary;

    // ─────────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class DailyRecord {
        private String date;          // "yyyy-MM-dd"
        private int dayOfMonth;       // 1..31
        private String dayName;       // "MON", "TUE", ...
        private int totalStudents;
        private int presentCount;
        private int absentCount;
        private int notMarkedCount;

        /**
         * Attendance percentage for this day (only when attendance exists).
         * null when no record exists for the day.
         */
        private Double attendancePercentage;

        /**
         * HIGH  → >80 %
         * MEDIUM → 60–80 %
         * LOW   → <60 %
         * NO_RECORD → no attendance data for that day
         */
        private String attendanceTag;

        /** List of students who were present on this day */
        private List<StudentRecord> presentStudents;

        /** List of students who were absent on this day */
        private List<StudentRecord> absentStudents;
    }

    @Data
    @Builder
    public static class StudentRecord {
        private Long   studentId;
        private String studentName;
        private String rollNo;
    }

    @Data
    @Builder
    public static class MonthlySummary {
        private int totalWorkingDays;       // days that have at least one attendance record
        private int totalStudents;
        private long totalPresent;          // sum of all present counts across working days
        private long totalAbsent;           // sum of all absent counts across working days

        /** Overall monthly attendance percentage */
        private double overallAttendancePercentage;

        /** Formatted, e.g. "87.5%" */
        private String overallAttendanceLabel;
    }
}
