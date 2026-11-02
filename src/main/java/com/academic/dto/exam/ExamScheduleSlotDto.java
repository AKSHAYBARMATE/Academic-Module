package com.academic.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleSlotDto {

    private Long id;

    @NotNull(message = "Exam date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate; // e.g. 2026-11-05

    @NotNull(message = "Class ID is required")
    private Long classId;
    private String className; // e.g. "3rd"

    @NotNull(message = "Section ID is required")
    private Long sectionId;
    private String sectionName; // e.g. "A"

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    private String subjectName; // e.g. "English"

    @NotBlank(message = "Start time is required")
    private String startTime; // e.g. "09:00"

    @NotBlank(message = "End time is required")
    private String endTime; // e.g. "11:00"

    private String duration; // e.g. "2 Hrs"
    private String room; // e.g. "R1"
    private Long invigilatorId; // Staff ID
    private String invigilatorName; // e.g. "Mr. Sharma"
    private Double maxMarks; // e.g. 100
    private String status; // "SCHEDULED", "COMPLETED", "CANCELLED"
}

