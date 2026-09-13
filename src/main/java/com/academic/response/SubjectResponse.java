package com.academic.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Integer id;

    // Both code and subjectCode provided for full compatibility
    private String code;
    private String subjectCode;

    // Both name and subjectName provided for full compatibility
    private String name;
    private String subjectName;

    private String department;
    private String program;
    private String semester;
    private Integer credits;
    private String type; // Theory, Practical, Elective
    private Integer hrsPerWeek;
    private String faculty;
    private List<String> faculties;
    private String academicYear;
    private String description;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
