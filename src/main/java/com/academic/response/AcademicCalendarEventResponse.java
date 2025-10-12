package com.academic.response;


import java.time.LocalDate;
import java.util.List;

public record AcademicCalendarEventResponse(
        Long id,
        String eventName,
        LocalDate date,
        String type,
        String classesInvolved, // comma-separated string for frontend
        List<Long> classIds,
        String duration,
        String status
) {}

