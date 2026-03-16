package com.academic.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubjectMarksResponse {

    private Long id;

    private Long subjectId;
    private String subjectName;

    private Double totalMarks;
    private Double totalMax;

    private String grade;

    private String subjectRemarks;

    private List<ComponentMarksResponse> components;
}