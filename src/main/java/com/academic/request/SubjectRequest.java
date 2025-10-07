package com.academic.request;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {
    private String subjectCode;
    private String subjectName;
    private String department;
    private Integer credits;
    private String type;
    private String status;
}
