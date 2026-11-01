package com.academic.controller;

import com.academic.request.ClassEventRequest;
import com.academic.response.ClassEventCheckResponse;
import com.academic.response.ClassEventResponse;
import com.academic.response.StandardResponse;
import com.academic.service.ClassEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller exposing CRUD and query APIs for Class & School Events.
 * Supports Student Attendance Class-Wise Event visibility (rendering "E" on event dates).
 */
@RestController
@RequestMapping("/api/v1/academic-module/class-events")
@RequiredArgsConstructor
public class ClassEventController {

    private final ClassEventService classEventService;

    /**
     * Create / Save a new Class Event (e.g. Parent-Teacher Meeting, Annual Function).
     * Corresponds to the "Save Event" action in the "Add Class Event" popup.
     */
    @PostMapping({"/saveEvent", "/createEvent"})
    public ResponseEntity<StandardResponse<ClassEventResponse>> saveEvent(
            @Valid @RequestBody ClassEventRequest request
    ) {
        StandardResponse<ClassEventResponse> response = classEventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing Class Event by ID.
     */
    @PutMapping("/updateEvent/{id}")
    public ResponseEntity<StandardResponse<ClassEventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody ClassEventRequest request
    ) {
        return ResponseEntity.ok(classEventService.updateEvent(id, request));
    }

    /**
     * Get Class Event details by ID.
     */
    @GetMapping({"/getEvent/{id}", "/{id}"})
    public ResponseEntity<StandardResponse<ClassEventResponse>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(classEventService.getEventById(id));
    }

    /**
     * Get paginated list of Class Events with optional filters.
     */
    @GetMapping({"/getAllEvents", ""})
    public ResponseEntity<StandardResponse<Page<ClassEventResponse>>> getAllEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String applicableTo,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(classEventService.getAllEvents(
                search, eventType, applicableTo, classId, sectionId, year, month, page, size
        ));
    }

    /**
     * Get unpaged list of events (filtered by year, month, class, section, session).
     */
    @GetMapping("/list")
    public ResponseEntity<StandardResponse<List<ClassEventResponse>>> getEventsList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String academicSession
    ) {
        return ResponseEntity.ok(classEventService.getEventsList(year, month, classId, sectionId, academicSession));
    }

    /**
     * Soft delete Class Event by ID.
     */
    @DeleteMapping({"/deleteEvent/{id}", "/{id}"})
    public ResponseEntity<StandardResponse<Map<String, Object>>> deleteEvent(@PathVariable Long id) {
        return ResponseEntity.ok(classEventService.deleteEvent(id));
    }

    /**
     * Check if a specific date is an event for attendance grid ("E" column).
     */
    @GetMapping("/checkIsEvent")
    public ResponseEntity<StandardResponse<ClassEventCheckResponse>> checkIsEvent(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId
    ) {
        return ResponseEntity.ok(classEventService.checkIsEvent(date, classId, sectionId));
    }

    /**
     * Get map of event dates for a month (used by Monthly Attendance Grid to highlight "E" column).
     */
    @GetMapping("/monthlyEventDates")
    public ResponseEntity<StandardResponse<Map<String, ClassEventCheckResponse>>> getMonthlyEventDates(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId
    ) {
        return ResponseEntity.ok(classEventService.getMonthlyEventDates(year, month, classId, sectionId));
    }
}

