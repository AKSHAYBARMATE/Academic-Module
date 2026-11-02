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
public class MarksEntryBatchRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private String assessmentComponent; // e.g. "Theory", "ALL"

    @NotEmpty(message = "Marks list cannot be empty")
    @Valid
    private List<StudentMarkItemDto> marksList;
}

