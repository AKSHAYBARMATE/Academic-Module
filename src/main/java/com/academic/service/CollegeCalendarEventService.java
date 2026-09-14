package com.academic.service;

import com.academic.request.CollegeCalendarEventRequest;
import com.academic.response.CollegeCalendarEventResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CollegeCalendarEventService {

    List<CollegeCalendarEventResponse> getAllEvents();

    List<CollegeCalendarEventResponse> listEvents(String academicYear, String eventType, String semester, String targetProgram, String status);

    Page<CollegeCalendarEventResponse> searchEvents(int page, int size, String search, String academicYear, String eventType, String semester, String targetProgram, String status);

    CollegeCalendarEventResponse getEventById(Long id);

    CollegeCalendarEventResponse createEvent(CollegeCalendarEventRequest request);

    CollegeCalendarEventResponse updateEvent(Long id, CollegeCalendarEventRequest request);

    void deleteEvent(Long id);

    List<CollegeCalendarEventResponse> bulkCreateEvents(List<CollegeCalendarEventRequest> requests);
}
