package com.academic.response;


import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSetupResponse {

    private Long id;
    private String examName;

    private Integer classId;
    private Integer subjectId;

    private LocalDate examDate;
    private Integer maxMarks;

    // Enriched from CommonMaster
    private String className;
    private String subjectName;
}
