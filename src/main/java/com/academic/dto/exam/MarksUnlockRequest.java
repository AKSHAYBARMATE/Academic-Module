package com.academic.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksUnlockRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Unlock reason is required")
    private String reason; // e.g. "Re-evaluation approved by Principal"
}

