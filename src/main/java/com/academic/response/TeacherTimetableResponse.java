package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherTimetableResponse {

    private Long teacherId;
    private String teacherName;
    private String staffCode;
    private String department;
    private Integer totalPeriods;
    private Integer workingDays;
    private Integer totalSchoolPeriods;
    private Integer freePeriods;
    private List<TeacherSlotDTO> slots;
    private List<TeacherSlotDTO> freeSlots;
    private Map<String, List<TeacherSlotDTO>> scheduleByDay;
    private Map<String, List<TeacherSlotDTO>> freeScheduleByDay;
    private Map<String, List<TeacherSlotDTO>> fullWeeklyGrid;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeacherSlotDTO {
        private Long slotId;
        private Integer day;          // 1 = Monday, 2 = Tuesday, ... 7 = Sunday
        private String dayName;       // "Monday", "Tuesday", etc.
        private String startTime;     // e.g. "09:00 AM"
        private String endTime;       // e.g. "10:00 AM"
        private Long subjectId;
        private String subjectName;
        private Long classId;
        private String className;
        private Long sectionId;
        private String sectionName;
        private String classSection;  // e.g. "Class 10 - A"
        private String room;
        private Long timetableId;
        private String timetableName;
        private Boolean isFree;       // true if free period, false if assigned
        private String status;        // "ASSIGNED" or "FREE"
    }
}
