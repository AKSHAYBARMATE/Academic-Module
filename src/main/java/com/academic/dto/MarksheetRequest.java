package com.academic.dto;


import com.academic.request.CoScholasticMarksRequest;
import com.academic.request.SubjectMarksRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarksheetRequest {

    /* STUDENT INFO */

    private Integer studentId;
    private Integer classId;
    private Integer sectionId;
    private Integer sessionId;
    private Integer examTypeId;

    private LocalDate examDate;

    /* SUBJECT MARKS */

    private List<SubjectMarksRequest> subjects;

    /* CO-SCHOLASTIC MARKS */

    private List<CoScholasticMarksRequest> coScholasticActivities;

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
}