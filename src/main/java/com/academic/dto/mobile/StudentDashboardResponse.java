package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentDashboardResponse {
    private String studentName;
    private String profilePicUrl;
    private String admissionNo;
    private AttendanceSummary attendance;
    private TodayScheduleSummary todaySchedule;
    private BusStatusSummary busStatus;

    @Data
    @Builder
    public static class AttendanceSummary {
        private String percentage;
        private String changePercent; // e.g. "+2% from last month"
        private boolean isPositive;
    }

    @Data
    @Builder
    public static class TodayScheduleSummary {
        private String nextClassSubject;
        private String room;
        private String startTime;
        private String endTime;
    }

    @Data
    @Builder
    public static class BusStatusSummary {
        private String status; // "En Route", "Approaching", etc.
        private String eta; // "03:45 PM"
        private String stopName;
    }
}
