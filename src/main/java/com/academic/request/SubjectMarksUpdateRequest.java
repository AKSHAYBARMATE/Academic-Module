package com.academic.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class SubjectMarksUpdateRequest {

    private Long id;           // exam_subject_config.id

    private Long subjectId;
    private Integer sessionId;
    private Integer examTypeId;
    private Integer classId;

    @JsonAlias("theory")
    private Integer theoryMarks;

    @JsonAlias("practical")
    private Integer practicalMarks;

    @JsonAlias("internal")
    private Integer internalMarks;

    private Integer theory;
    private Integer practical;
    private Integer internal;

    public Integer getTheoryMarks() {
        return theoryMarks != null ? theoryMarks : theory;
    }

    public Integer getPracticalMarks() {
        return practicalMarks != null ? practicalMarks : practical;
    }

    public Integer getInternalMarks() {
        return internalMarks != null ? internalMarks : internal;
    }

    private List<ComponentConfigRequest> components;
}
