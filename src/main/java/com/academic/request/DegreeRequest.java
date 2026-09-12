package com.academic.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DegreeRequest {
    private String code;
    private String name;
    private String degreeType;
    private String duration;
    private Integer totalSemesters;
    private String description;
    private String status;
}
