package com.academic.dto;

import lombok.Data;

@Data
public class SubjectMarksRequest {

    private Long subjectId;

    private Integer theoryMarks;
    private Integer theoryMax;

    private Integer practicalMarks;
    private Integer practicalMax;

    private Integer internalMarks;
    private Integer internalMax;

    private String subjectRemarks;
}
