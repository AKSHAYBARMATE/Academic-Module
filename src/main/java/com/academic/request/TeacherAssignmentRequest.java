package com.academic.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentRequest {
    private String teacherName;
    private String employeeId;
    private String subject;
    private String classes;
    private Integer loadHours;
    private String status;
}
