package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSectionResponse {
    private Long id;
    private String className;
    private String classId;
    private String section;
    private String classTeacher;
    private Integer students;
    private String roomNo;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
