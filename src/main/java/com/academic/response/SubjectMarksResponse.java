package com.academic.response;

import com.academic.dto.ComponentMarksResponse;
import lombok.Data;

import java.util.List;

@Data
public class SubjectMarksResponse {

    private Integer id;
    private Integer subjectId;
    private String subjectName;

    private Integer theoryMarks;
    private Integer theoryMax;

    private Integer practicalMarks;
    private Integer practicalMax;

    private Integer internalMarks;
    private Integer internalMax;

    private Integer totalMarks;
    private Integer totalMax;

    private String grade;

    private String subjectRemarks;

    private List<ComponentMarksResponse> components;
}
