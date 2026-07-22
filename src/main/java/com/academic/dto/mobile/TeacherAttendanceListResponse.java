package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TeacherAttendanceListResponse {
    private String classSectionName;
    private Long classId;
    private Long sectionId;
    private String date;
    private List<StudentAttendanceDto> students;
    private AttendanceSummary summary;

    @Data
    @Builder
    public static class StudentAttendanceDto {
        private Long studentId;
        private String studentName;
        private String rollNo;
        private String profilePicUrl;
        private String status; // "PRESENT", "ABSENT", "NOT_MARKED"
    }

    @Data
    @Builder
    public static class AttendanceSummary {
        private int total;
        private int present;
        private int absent;
    }
}
