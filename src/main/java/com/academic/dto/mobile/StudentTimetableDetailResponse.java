package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentTimetableDetailResponse {
    private String dayName;
    private String date;
    private List<ScheduleSlotDto> schedule;

    @Data
    @Builder
    public static class ScheduleSlotDto {
        private String subjectName;
        private String teacherName;
        private String room;
        private String startTime;
        private String endTime;
        private boolean isLive;
        private String joinUrl;
        private String status; // "COMPLETED", "LIVE", "UPCOMING"
    }
}
