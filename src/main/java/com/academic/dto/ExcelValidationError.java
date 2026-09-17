package com.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelValidationError {
    private int rowNumber;
    private String columnName;
    private String invalidValue;
    private String errorMessage;
}
