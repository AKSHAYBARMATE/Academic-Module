package com.academic.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksGridLoadResponse {

    private Long examId;
    private String examName;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private String assessmentComponent; // e.g. "Theory", "ALL"
    private List<String> assessmentComponents; // Configured components for this exam, e.g. ["Theory", "Practical", "Internal", "Viva"]
    private java.util.Map<String, Double> componentMaxMarks; // Dynamic max marks per component e.g. {"Theory": 70, "Practical": 10, "Internal": 20, "Viva": 10}

    // Configured maximum marks for table headers: Theory (70) | Practical (10) | Internal (20) | Total (100)
    private Double theoryMaxMarks;
    private Double practicalMaxMarks;
    private Double internalMaxMarks;
    private Double totalMaxMarks;
    private Double passingMarks;
    private Double weightagePercentage;

    // Workflow status
    private String submissionStatus; // "DRAFT", "VALIDATED", "SUBMITTED", "LOCKED"
    private boolean isLocked;
    private Long lockedByUser;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lockedAt;

    private Long submittedByUser;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime submittedAt;

    // Student rows
    private List<StudentMarkItemDto> students;
}

