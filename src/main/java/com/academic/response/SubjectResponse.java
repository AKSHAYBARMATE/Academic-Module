package com.academic.response;

import lombok.*;

import java.time.LocalDateTime;

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
    private String departmentName;

    private Integer credits;
    private String type; // Theory, Practical, Elective, Core
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getIdAsLong() {
        return this.id != null ? this.id.longValue() : null;
    }
}
