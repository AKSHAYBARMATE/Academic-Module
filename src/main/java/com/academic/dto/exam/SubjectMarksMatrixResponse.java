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
public class SubjectMarksMatrixResponse {

    private Long examId;
    private String examName;
    private Long classId;
    private String className;
    private List<String> assessmentComponents; // e.g. ["Theory", "Practical", "Internal", "Viva"]
    private List<SubjectMarksMatrixItemDto> matrixItems;
}

