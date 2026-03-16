package com.academic.request;

import lombok.Data;

import java.util.List;

@Data
public class SubjectMarksUpdateRequest {

    private Long id;           // exam_subject_config.id

    private Long subjectId;
    private Integer sessionId;
    private Integer examTypeId;
    private Integer classId;

    private List<ComponentConfigRequest> components;
}
