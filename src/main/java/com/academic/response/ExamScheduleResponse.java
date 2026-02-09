package com.academic.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ExamScheduleResponse {

    private Long id;
    private String examTitle;

    private String session;
    private String examType;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
}
