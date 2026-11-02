package com.academic.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotEmpty(message = "Schedule slots cannot be empty")
    @Valid
    private List<ExamScheduleSlotDto> scheduleSlots;
}

