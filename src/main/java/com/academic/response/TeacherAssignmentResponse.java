package com.academic.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentResponse {
    private Long id;
    private String teacherName;
    private String employeeId;
    private String subject;
    private String classes;
    private Integer loadHours;
    private String status;
}
