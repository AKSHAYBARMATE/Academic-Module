package com.academic.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicExamRequest {

    private Long id;

    @NotBlank(message = "Exam name is required")
    private String examName; // e.g., "Unit Test 1"

    @NotBlank(message = "Exam type is required")
    private String examType; // e.g., "Unit Test", "Half Yearly", "Annual", "Term 1"

    @NotBlank(message = "Academic session is required")
    private String academicSession; // e.g., "2026-27"

    private Long sessionId;

    @NotNull(message = "At least one class must be selected")
    private List<Long> classIds; // e.g., [3, 4, 5]

    /**
     * Assessment components configured for this exam, e.g. ["Theory", "Practical", "Internal"] or any custom component added by the user.
     * Defaults to ["Theory", "Practical", "Internal"] if not provided.
     */
    private List<String> assessmentComponents;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String status; // "Draft", "Published", "Completed", "Cancelled"

    private String description;
}

