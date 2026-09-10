package com.academic.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class SubjectMarksRequest {

    private Integer subjectId;


    private Integer theoryMarks;


    private Integer practicalMarks;

    private Integer internalMarks;

    private List<ComponentMarksRequest> components;

    private String subjectRemarks;
}
