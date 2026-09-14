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
public class CollegeCalendarEventRequest {

    private Long id;

    @NotBlank(message = "Event title is required")
    private String title;

    @NotBlank(message = "Event type / category is required")
    private String eventType; // Academic, Examination, Submission, Holiday, Activity

    private String academicYear; // e.g. "2025-2026"

    private String semester; // e.g. "All Semesters", "Semester 1", "Semester 3"

    private String startDate; // YYYY-MM-DD

    private String date; // Alias for startDate if sent as date

    private String endDate; // YYYY-MM-DD (optional)

    private String status; // Scheduled, Active, Completed

    private String targetProgram; // e.g. "All Programs", "B.Tech (CSE)", "BCA"

    private String description;

    public String getEffectiveStartDate() {
        if (startDate != null && !startDate.isBlank()) {
            return startDate.trim();
        }
        if (date != null && !date.isBlank()) {
            return date.trim();
        }
        return "";
    }
}
