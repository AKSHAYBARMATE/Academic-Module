package com.academic.mapper;


import com.academic.entity.AcademicCalendarEvent;
import com.academic.request.AcademicCalendarEventRequest;
import com.academic.response.AcademicCalendarEventResponse;

// Static utility class to convert between Entity and DTO
public class AcademicCalendarEventMapper {

    // Convert Request DTO to Entity for creation
    public static AcademicCalendarEvent toEntity(AcademicCalendarEventRequest request) {
        AcademicCalendarEvent entity = new AcademicCalendarEvent();
        entity.setEventName(request.eventName());
        entity.setDate(request.date());
        entity.setType(request.type());
        entity.setClassesInvolved(request.classesInvolved());
        entity.setDuration(request.duration());
        entity.setStatus(request.status());
        // isDeleted is false by default
        return entity;
    }

    // Convert Entity to Response DTO for reading
    public static AcademicCalendarEventResponse toResponse(AcademicCalendarEvent entity) {
        return new AcademicCalendarEventResponse(
                entity.getId(),
                entity.getEventName(),
                entity.getDate(),
                entity.getType(),
                entity.getClassesInvolved(),
                entity.getDuration(),
                entity.getStatus()
        );
    }
}
