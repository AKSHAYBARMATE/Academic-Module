package com.academic.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksValidationResponse {

    private boolean isValid;
    private int totalStudents;
    private int validStudents;
    private int invalidStudents;
    private List<ValidationErrorItem> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationErrorItem {
        private Long studentId;
        private String rollNo;
        private String studentName;
        private String field; // "theoryMarks", "practicalMarks", "internalMarks"
        private String message; // e.g. "Theory marks (75) exceeds configured max marks (70)"
    }
}

