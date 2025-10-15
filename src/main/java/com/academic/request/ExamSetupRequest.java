package com.academic.request;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSetupRequest {

    private String examName;

    // These IDs come from CommonMaster (Class, Subject)
    private Integer classId;
    private Integer subjectId;

    private LocalDate examDate;

    private Integer maxMarks;

    private Integer academicYearId;
}
