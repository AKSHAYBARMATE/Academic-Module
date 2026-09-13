package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeTimetableRequest {

    private Integer degreeId;
    private String degreeCode;

    @NotBlank(message = "Program is required")
    private String program;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotBlank(message = "Section is required")
    private String section;

    private String academicYear;

    @NotBlank(message = "Day of week is required")
    private String day; // Monday, Tuesday, Wednesday, etc.

    @NotBlank(message = "Time slot is required")
    private String timeSlot; // "09:00 - 10:00 AM"

    private String periodLabel; // "Period 1", "Morning Refreshment"

    private String startTime;
    private String endTime;

    private Integer durationMinutes;
    private Boolean isBreak;

    // Subject mapping
    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    // Staff mapping
    private Integer staffId;
    private String teacherName;
    private String staffCode;

    @NotBlank(message = "Room / Venue is required")
    private String room; // "Room 101", "Computer Lab 1"

    private String type; // Theory, Practical, Mentoring, Library, Activity
    private Integer spanPeriods; // 1 or 2
    private String status; // Active, Draft
}
