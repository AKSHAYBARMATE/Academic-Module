package com.academic.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleResponse {

    private Long examId;
    private String examName;
    private String academicSession;
    private String examStatus;
    private List<ExamScheduleSlotDto> slots;
}

