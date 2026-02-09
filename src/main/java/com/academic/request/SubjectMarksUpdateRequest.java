package com.academic.request;

import lombok.Data;

@Data
public class SubjectMarksUpdateRequest {

    private Long id;   // exam_subject_config.id

    private Integer theoryMarks;
    private Integer practicalMarks;
    private Integer internalMarks;
}
