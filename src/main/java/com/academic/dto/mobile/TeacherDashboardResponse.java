package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TeacherDashboardResponse {
    private String teacherName;
    private String profilePicUrl;
    private String date;
    private PunchStatusResponse punchStatus;
    private DailySummaryResponse summary;
    private List<AnnouncementDto> announcements;
    private List<TeacherScheduleSlotDto> todaySchedule;

    @Data
    @Builder
    public static class PunchStatusResponse {
        private String status; // "Punched In", "Punched Out"
        private String punchTime; // "08:45 AM"
        private boolean canPunchOut;
    }

    @Data
    @Builder
    public static class DailySummaryResponse {
        private int totalClasses;
        private String attendancePercentage; // e.g. "85% Done"
    }

    @Data
    @Builder
    public static class AnnouncementDto {
        private String type; // "SCHOOL EVENT", "ADMIN NOTE"
        private String title;
        private String content;
        private String date;
    }

    @Data
    @Builder
    public static class TeacherScheduleSlotDto {
        private Long id;
        private String subjectName;
        private String classSection; // "Grade 10-B"
        private String timeRange; // "9:00 - 10:00 AM"
        private String room;
        private String status; // "ONGOING", "UPCOMING", "COMPLETED"
        private boolean attendanceMarked;
    }
}
