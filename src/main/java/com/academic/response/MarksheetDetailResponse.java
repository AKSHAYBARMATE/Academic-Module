package com.academic.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarksheetDetailResponse {

    private Integer id;

    /* STUDENT */

    private Integer studentId;
    private String studentName;

    private Integer classId;
    private String className;

    private Integer sectionId;
    private String sectionName;

    private Integer sessionId;
    private String sessionName;

    /* EXAM */

    private Integer examTypeId;
    private String examTypeName;

    private Integer termNumber;

    private LocalDate examDate;

    /* SUBJECTS */

    private List<SubjectMarksResponse> subjects;

    /* CO SCHOLASTIC */

    private List<CoScholasticResponse> coScholasticActivities;

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