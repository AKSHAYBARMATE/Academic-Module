package com.academic.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherExamScheduleResponse {
    private String examTitle;
    private String subTitle; // e.g., "Final Term Exams - 2024"
    private Integer classId;
    private Integer examTypeId;
    private List<ExamSlotDto> exams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamSlotDto {
        private String date; // "Oct 12, Monday"
        private Integer subjectId;
        private String subjectName;
        private String subjectCode;
        private String timeRange; // "10:00 AM - 01:00 PM"
        private String location; // "Hall B, 2nd Floor"
        private String syllabusUrl;
        private String status; // "UPCOMING", "COMPLETED"
    }
}
