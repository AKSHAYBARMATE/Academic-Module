package com.academic.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRequest {
    private String code;
    private String name;
    private String degreeCode;
    private String degreeType;
    private String duration;
    private Integer totalSemesters;
    private Integer intake;
    private String accreditation;
    private String description;
    private String status;
}
