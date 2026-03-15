package com.academic.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExamSubjectConfigResponse {

    private Long id;

    private String session;

    private Integer examTypeId;
    private String examType;

    private Integer classId;
    private String className;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    private List<ComponentConfigResponse> components;
}
