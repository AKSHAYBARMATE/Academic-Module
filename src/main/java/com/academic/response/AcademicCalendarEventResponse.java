package com.academic.response;


import java.time.LocalDate;

// DTO for returning academic calendar event details
public record AcademicCalendarEventResponse(
        Long id,
        String eventName,
        LocalDate date,
        String type,
        String classesInvolved,
        String duration,
        String status
) {}
