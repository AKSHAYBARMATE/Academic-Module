package com.academic.request;

import lombok.Data;

import java.util.List;

@Data
public class SubjectMarksRequest {

    private Integer subjectId;

    private List<ComponentMarksRequest> components;

    private String subjectRemarks;
}
