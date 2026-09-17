package com.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeMarksheetResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String universityPrn;
    private String universityRollNo;
    private String collegeRollNo;
    private String admissionNo;
    private String fatherName;
    private String motherName;

    private String degreeCode;
    private String programCode;
    private String programName;
    private String departmentName;

    private String academicYear;
    private String semester;
    private String examSession;
    private String examType;

    private Double totalCreditsOffered;
    private Double totalCreditsEarned;

    private Double sgpa;
    private Double cgpa;

    private Double totalMarksObtained;
    private Double totalMaxMarks;
    private Double percentage;

    private String resultStatus;
    private Integer backlogCount;

    private String marksheetNumber;
    private LocalDate issueDate;
    private String verificationCode;

    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CollegeMarksheetSubjectResponse> subjects;
}
