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
    private Integer classId;
    private Integer section;
    private String classTeacher;
    private Integer students;
    private String roomNo;
    private Boolean active;
}
