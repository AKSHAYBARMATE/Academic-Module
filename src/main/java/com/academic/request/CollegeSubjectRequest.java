package com.academic.request;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeSubjectRequest {

    // Supports both 'code' and 'subjectCode'
    private String code;
    private String subjectCode;

    // Supports both 'name' and 'subjectName'
    private String name;
    private String subjectName;

    // Foreign Keys
    private Integer degreeId;
    private Integer departmentId;

    // Convenience string values
    private String degree;
    private String department;

    private String program;
    private String semester;
    private Integer credits;
    private String type; // Theory, Practical, Elective, Core
    private Integer hrsPerWeek;
    private String faculty;
    private List<String> faculties;
    private String academicYear;
    private String description;
    private String status;

    public String getResolvedCode() {
        if (this.code != null && !this.code.trim().isBlank()) {
            return this.code.trim();
        }
        if (this.subjectCode != null && !this.subjectCode.trim().isBlank()) {
            return this.subjectCode.trim();
        }
        return null;
    }

    public String getResolvedName() {
        if (this.name != null && !this.name.trim().isBlank()) {
            return this.name.trim();
        }
        if (this.subjectName != null && !this.subjectName.trim().isBlank()) {
            return this.subjectName.trim();
        }
        return null;
    }
}
