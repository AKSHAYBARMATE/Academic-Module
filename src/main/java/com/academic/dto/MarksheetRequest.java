package com.academic.dto;


import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarksheetRequest {

    private Long studentId;
    private Integer classId;
    private Integer sectionId;
    private Integer sessionId;
    private Integer examTypeId;
    private LocalDate examDate;

    private List<SubjectMarksRequest> subjects;

    private Integer totalMarksObtained;
    private Integer totalMaxMarks;
    private Double percentage;
    private String grade;
    private Double gpa;

    private Integer attendanceDays;
    private Integer totalWorkingDays;
    private String conductGrade;
    private String sportsGrade;
    private String extraCurricularGrade;
    private String teacherRemarks;
    private String principalRemarks;
}
