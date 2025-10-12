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
    private String className;     // resolved from common master
    private Integer classId;      // original ID
    private String sectionName;   // resolved from common master
    private Integer sectionId;    // original ID
    private String classTeacher;
    private Integer students;
    private String roomNo;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

