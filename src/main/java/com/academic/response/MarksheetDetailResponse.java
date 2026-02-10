package com.academic.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarksheetDetailResponse {

    private Long id;
    private Long studentId;
    private Integer classId;
    private Integer sectionId;
    private Integer sessionId;
    private Integer examTypeId;
    private LocalDate examDate;

    /* SUBJECTS */
    private List<SubjectMarksResponse> subjects;

    /* SUMMARY */
    private Integer totalMarksObtained;
    private Integer totalMaxMarks;
    private Double percentage;
    private String grade;
    private Double gpa;

    /* EVALUATION */
    private Integer attendanceDays;
    private Integer totalWorkingDays;
    private String conductGrade;
    private String sportsGrade;
    private String extraCurricularGrade;
    private String teacherRemarks;
    private String principalRemarks;

    private Boolean published;
}
