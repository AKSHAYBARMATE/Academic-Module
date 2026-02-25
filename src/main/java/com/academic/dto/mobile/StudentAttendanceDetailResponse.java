package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentAttendanceDetailResponse {
    private String totalPercentage;
    private String changeVsLastMonth;
    private int presentDays;
    private int absentDays;
    private int holidays;
    private String minimumRequiredPercentage;
    private String progressStatus; // e.g., "Good Progress"
    private List<DailyAttendanceDto> calendar;
    private List<RecentAbsenceDto> recentAbsences;

    @Data
    @Builder
    public static class DailyAttendanceDto {
        private String date; // ISO Format yyyy-MM-dd
        private String status; // "PRESENT", "ABSENT", "HOLIDAY", "NOT_MARKED"
    }

    @Data
    @Builder
    public static class RecentAbsenceDto {
        private String date;
        private String reason;
    }
}
