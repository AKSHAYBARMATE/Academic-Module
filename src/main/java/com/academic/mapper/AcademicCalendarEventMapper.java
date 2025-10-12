package com.academic.mapper;


import com.academic.entity.AcademicCalendarEvent;
import com.academic.request.AcademicCalendarEventRequest;
import com.academic.response.AcademicCalendarEventResponse;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Static utility class to convert between Entity and DTO
public class AcademicCalendarEventMapper {

    public static AcademicCalendarEvent toEntity(AcademicCalendarEventRequest request) {
        AcademicCalendarEvent entity = new AcademicCalendarEvent();
        entity.setEventName(request.eventName());
        entity.setDate(request.date());
        entity.setType(request.type());
        entity.setClassesInvolved(request.classIds()); // List<Long>
        entity.setDuration(request.duration());
        entity.setStatus(request.status());
        return entity;
    }


    // For getById (or when you only have IDs)
    public static AcademicCalendarEventResponse toResponse(AcademicCalendarEvent entity) {
        return new AcademicCalendarEventResponse(
                entity.getId(),
                entity.getEventName(),
                entity.getDate(),
                entity.getType(),
                "", // no names, only IDs
                entity.getClassesInvolved(),
                entity.getDuration(),
                entity.getStatus()
        );
    }

    // For list (resolve names from common master)
    public static AcademicCalendarEventResponse toResponse(AcademicCalendarEvent entity, Map<Long, String> classIdToNameMap) {
        String classNames = entity.getClassesInvolved().stream()
                .map(classIdToNameMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        return new AcademicCalendarEventResponse(
                entity.getId(),
                entity.getEventName(),
                entity.getDate(),
                entity.getType(),
                classNames,
                entity.getClassesInvolved(), // original IDs
                entity.getDuration(),
                entity.getStatus()
        );
    }




}
