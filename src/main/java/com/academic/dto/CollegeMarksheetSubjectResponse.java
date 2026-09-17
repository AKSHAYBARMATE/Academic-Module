package com.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeMarksheetSubjectResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private String subjectType;
    private Double credits;
    private Integer internalMaxMarks;
    private Integer internalObtainedMarks;
    private Integer externalMaxMarks;
    private Integer externalObtainedMarks;
    private Integer practicalMaxMarks;
    private Integer practicalObtainedMarks;
    private Integer totalMarks;
    private Integer maxTotalMarks;
    private Double gradePoint;
    private String letterGrade;
    private String status;
    private Boolean isBacklog;
    private Integer attemptNumber;
}
