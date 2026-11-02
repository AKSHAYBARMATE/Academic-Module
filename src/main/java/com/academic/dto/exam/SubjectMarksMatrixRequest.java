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
public class SubjectMarksMatrixRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    private Long classId; // optional class override

    @NotEmpty(message = "Subject matrix items cannot be empty")
    @Valid
    private List<SubjectMarksMatrixItemDto> matrixItems;
}

