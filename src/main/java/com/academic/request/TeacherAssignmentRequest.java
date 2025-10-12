package com.academic.request;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentRequest {
    private String teacherName;
    private String employeeId;
    private String subject;
    private List<Long> classIds; // List of class IDs from CommonMaster
    private Integer loadHours;
    private String status;
}