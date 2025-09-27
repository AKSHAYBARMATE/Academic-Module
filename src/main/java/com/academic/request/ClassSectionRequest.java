package com.academic.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSectionRequest {
    private String className;
    private String classId;
    private String section;
    private String classTeacher;
    private Integer students;
    private String roomNo;
    private Boolean active;
}
