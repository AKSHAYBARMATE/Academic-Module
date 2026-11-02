package com.academic.dto.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicExamResponse {

    private Long id;
    private String examName;
    private String examType;
    private String academicSession;
    private Long sessionId;
    private List<Long> classIds;
    private List<String> classNames; // e.g. ["3rd", "4th", "5th"]
    private String classesLabel; // e.g. "3rd, 4th, 5th"
    private List<String> assessmentComponents; // e.g. ["Theory", "Practical", "Internal", ...]

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String status; // "Draft", "Published", "Completed", "Cancelled"
    private String description;

    private Long createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdOn;
    private Long modifiedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime modifiedAt;
}

