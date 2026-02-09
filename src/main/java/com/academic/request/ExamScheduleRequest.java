package com.academic.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExamScheduleRequest {

    private String examTitle;
    private Long sessionId;
    private Integer examTypeId;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status; // DRAFT / PUBLISHED
}
