package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {
    private Integer id;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
