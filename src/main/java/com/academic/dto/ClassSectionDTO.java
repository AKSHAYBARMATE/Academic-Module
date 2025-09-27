package com.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSectionDTO {
    private Long id;
    private String className;
    private String section;
    private String classTeacher;
    private Integer students;
    private String roomNo;
    private Boolean active;
}
