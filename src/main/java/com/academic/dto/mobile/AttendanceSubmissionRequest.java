package com.academic.dto.mobile;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceSubmissionRequest {
    private Long classId;
    private Long sectionId;
    private LocalDate date;
    private List<StudentStatus> attendanceList;

    @Data
    public static class StudentStatus {
        private Long studentId;
        private String status; // "PRESENT", "ABSENT"
    }
}
