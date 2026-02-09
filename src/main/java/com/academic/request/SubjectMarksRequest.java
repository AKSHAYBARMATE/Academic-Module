package com.academic.request;

import lombok.Data;

@Data
public class SubjectMarksRequest {

    private Long subjectId;
    private Integer theoryMarks;
    private Integer practicalMarks;
    private Integer internalMarks;
}
