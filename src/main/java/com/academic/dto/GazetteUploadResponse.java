package com.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GazetteUploadResponse {
    private boolean success;
    private String message;
    private int totalRowsProcessed;
    private int validStudentsCount;
    private int totalSubjectsParsed;
    private int errorCount;

    @Builder.Default
    private List<ExcelValidationError> errors = new ArrayList<>();
}
