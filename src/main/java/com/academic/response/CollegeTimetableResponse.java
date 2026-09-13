package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeTimetableResponse {

    private Long id;

    private Integer degreeId;
    private String degreeCode;
    private String degreeName;

    private String program;
    private String semester;
    private String section;
    private String academicYear;

    private String day;
    private String timeSlot;
    private String periodLabel;
    private String startTime;
    private String endTime;
    private Integer durationMinutes;
    private Boolean isBreak;

    // Subject Details
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String subjectType;
    private Integer subjectCredits;

    // Staff Details
    private Integer staffId;
    private String staffCode;
    private String teacherName;
    private String staffDepartment;
    private String staffDesignation;
    private String staffEmail;

    // Venue & Type
    private String room;
    private String type;
    private Integer spanPeriods;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
