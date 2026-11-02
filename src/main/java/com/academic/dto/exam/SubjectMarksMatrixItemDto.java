package com.academic.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMarksMatrixItemDto {

    private Long id;
    private Long classId; // optional

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private String subjectName; // e.g. "English", "Hindi", "Mathematics"

    private Double theoryMaxMarks; // e.g. 70
    private Double practicalMaxMarks; // e.g. 10
    private Double internalMaxMarks; // e.g. 20
    private Double totalMaxMarks; // e.g. 100
    private Double passingMarks; // e.g. 33
    private Double weightagePercentage; // e.g. 100 (%)

    // Dynamic map of component name -> max marks (e.g. {"Theory": 70, "Practical": 10, "Internal": 20, "Viva": 10})
    private java.util.Map<String, Double> componentMaxMarks;
}

