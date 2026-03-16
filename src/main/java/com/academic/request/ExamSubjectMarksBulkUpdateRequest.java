package com.academic.request;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubjectMarksBulkUpdateRequest {

    private Integer sessionId;
    private Integer examTypeId;
    private Integer classId;

    private List<SubjectMarksUpdateRequest> subjects;

    private List<CoScholasticUpdateRequest> coScholasticActivities;

}
