package com.academic.dto.mobile;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentExamResultsResponse {
    private String academicYear;
    private List<SubjectResultDto> results;
    private OverallPerformanceDto summary;

    @Data
    @Builder
    public static class SubjectResultDto {
        private String subjectName;
        private int marksObtained;
        private int maxMarks;
        private String grade;
    }

    @Data
    @Builder
    public static class OverallPerformanceDto {
        private String termName;
        private String totalPercentage;
        private String resultStatus; // "PASSED", "FAILED"
        private String classRank; // e.g., "4th of 45 students"
    }
}
