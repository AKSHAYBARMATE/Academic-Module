package com.academic.request;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubjectMarksBulkUpdateRequest {

    private List<SubjectMarksUpdateRequest> subjects;
}
