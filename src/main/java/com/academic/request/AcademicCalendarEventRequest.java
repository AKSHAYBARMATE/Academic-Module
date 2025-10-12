package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

// DTO for creating and updating an academic calendar event
public record AcademicCalendarEventRequest(
        @NotBlank(message = "Event Name is required")
        String eventName,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Type is required")
        String type,

        @NotEmpty(message = "At least one class is required")
        List<Long> classIds, // from dropdown

        String duration,

        @NotBlank(message = "Status is required")
        String status
) {}
