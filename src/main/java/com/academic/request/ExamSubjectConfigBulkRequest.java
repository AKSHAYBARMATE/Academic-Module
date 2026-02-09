package com.academic.request;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubjectConfigBulkRequest {

    private Integer sessionId;
    private Integer examTypeId;

    /** Multiple subjects in one save */
    private List<SubjectMarksRequest> subjects;
}
