package com.academic.request;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubjectConfigBulkRequest {

    private Integer sessionId;
    private Integer examTypeId;
    private Integer classId;
    private List<SubjectMarksRequest> subjects;

    private List<CoScholasticUpdateRequest> coScholasticActivities;
}
