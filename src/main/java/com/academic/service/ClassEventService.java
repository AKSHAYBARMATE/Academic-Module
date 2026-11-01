package com.academic.service;

import com.academic.request.ClassEventRequest;
import com.academic.response.ClassEventCheckResponse;
import com.academic.response.ClassEventResponse;
import com.academic.response.StandardResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ClassEventService {

    StandardResponse<ClassEventResponse> createEvent(ClassEventRequest request);

    StandardResponse<ClassEventResponse> updateEvent(Long id, ClassEventRequest request);

    StandardResponse<ClassEventResponse> getEventById(Long id);

    StandardResponse<Page<ClassEventResponse>> getAllEvents(
            String search,
            String eventType,
            String applicableTo,
            Long classId,
            Long sectionId,
            Integer year,
            Integer month,
            int page,
            int size
    );

    StandardResponse<List<ClassEventResponse>> getEventsList(
            Integer year,
            Integer month,
            Long classId,
            Long sectionId,
            String academicSession
    );

    StandardResponse<Map<String, Object>> deleteEvent(Long id);

    StandardResponse<ClassEventCheckResponse> checkIsEvent(LocalDate date, Long classId, Long sectionId);

    StandardResponse<Map<String, ClassEventCheckResponse>> getMonthlyEventDates(int year, int month, Long classId, Long sectionId);
}

