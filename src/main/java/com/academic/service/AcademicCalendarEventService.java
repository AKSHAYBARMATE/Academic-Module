package com.academic.service;



import com.academic.request.AcademicCalendarEventRequest;
import com.academic.response.AcademicCalendarEventResponse;

import java.time.LocalDate;
import java.util.List;

public interface AcademicCalendarEventService {

    // --- C - CREATE ---
    AcademicCalendarEventResponse create(AcademicCalendarEventRequest request);

    List<AcademicCalendarEventResponse> findAll(String search, String type, String status, LocalDate date);

    // --- R - READ BY ID ---
    AcademicCalendarEventResponse findById(Long id);

    // --- U - UPDATE ---
    AcademicCalendarEventResponse update(Long id, AcademicCalendarEventRequest request);

    // --- D - DELETE (Soft Delete) ---
    void delete(Long id);
}
