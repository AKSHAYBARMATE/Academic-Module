package com.academic.request;

import lombok.Data;

@Data
public class SubjectMarksUpdateRequest {

    private Long id;   // exam_subject_config.id
    private Long subjectId;   // exam_subject_config.id
    private Long sessionId;   // exam_subject_config.id
    private Integer examTypeId;   // exam_subject_config.id
    private Integer classId;   // exam_subject_config.id

    private Integer theoryMarks;
    private Integer practicalMarks;
    private Integer internalMarks;
}
