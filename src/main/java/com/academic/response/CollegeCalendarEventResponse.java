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
public class CollegeCalendarEventResponse {

    private Long id;
    private String title;
    private String eventType;
    private String academicYear;
    private String semester;
    private String startDate;
    private String date; // Alias for frontend convenience
    private String endDate;
    private String status;
    private String targetProgram;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
