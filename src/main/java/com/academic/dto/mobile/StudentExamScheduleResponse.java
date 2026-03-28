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
public class StudentExamScheduleResponse {

    private String examTitle; // e.g. "Annual Examination 2025-26"
    private String examType; // e.g. "Annual / Half Yearly"
    private String academicYear; // e.g. "2025-26"
    private String startDate;
    private String endDate;
    private String status; // DRAFT / PUBLISHED

    private List<SubjectExamDto> subjects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectExamDto {
        private String subjectName;
        private String examDate;
        private List<ComponentDto> components;
        private Integer totalMarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentDto {
        private String componentName;
        private Integer maxMarks;
    }
}
