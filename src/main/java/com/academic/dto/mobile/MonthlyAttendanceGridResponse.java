package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MonthlyAttendanceGridResponse {
    private String classSectionName;
    private String monthLabel;
    private int month;
    private int year;
    private List<DayHeader> dayHeaders;
    private List<StudentRow> rows;

    @Data
    @Builder
    public static class DayHeader {
        private String date;
        private String dayOfMonth;
        private String dayName;
        private boolean weekend;
    }

    @Data
    @Builder
    public static class StudentRow {
        private Long studentId;
        private String studentName;
        private String attendancePercentage;
        private int presentCount;
        private int lateCount;
        private int absentCount;
        private int holidayCount;
        private int halfDayCount;
        private Map<Integer, String> dailyStatus;
    }
}
