package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarksheetResponse {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String className;
    private String sectionName;
    private String sessionName;
    private String examTypeName;
    private LocalDate examDate;
    private Integer totalMarksObtained;
    private Integer totalMaxMarks;
    private Double percentage;
    private String grade;
    private Boolean published;
}
