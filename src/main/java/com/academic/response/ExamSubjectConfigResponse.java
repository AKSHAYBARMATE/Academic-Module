package com.academic.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamSubjectConfigResponse {

    private Long id;
    private String session;
    private String examType;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    private Integer theoryMarks;
    private Integer practicalMarks;
    private Integer internalMarks;
    private Integer totalMarks;
}
