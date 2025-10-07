package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// DTO for creating and updating an academic calendar event
public record AcademicCalendarEventRequest(
        @NotBlank(message = "Event Name is required")
        String eventName,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Type is required")
        String type, // e.g., Examination, Event

        String classesInvolved, // Optional

        String duration, // Optional

        @NotNull(message = "Status is required")
        String status

) {
}
