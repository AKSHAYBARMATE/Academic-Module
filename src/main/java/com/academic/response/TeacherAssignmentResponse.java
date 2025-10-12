package com.academic.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentResponse {
    private Long id;
    private String teacherName;
    private String employeeId;
    private String subject;
    private List<Long> classIds;        // IDs
    private String classNames;             // Human-readable names (comma-separated)
    private Integer loadHours;
    private String status;
}
