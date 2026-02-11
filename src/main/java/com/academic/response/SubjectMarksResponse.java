package com.academic.response;

import lombok.Data;

@Data
public class SubjectMarksResponse {

    private Long id;
    private Long subjectId;
    private String subjectName;

    private Integer theoryMarks;
    private Integer theoryMax;

    private Integer practicalMarks;
    private Integer practicalMax;

    private Integer internalMarks;
    private Integer internalMax;

    private Integer totalMarks;
    private Integer totalMax;

    private String subjectRemarks;
}
