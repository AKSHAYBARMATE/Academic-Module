package com.academic.controller;

import com.academic.request.CollegeCalendarEventRequest;
import com.academic.response.CollegeCalendarEventResponse;
import com.academic.response.StandardResponse;
import com.academic.service.CollegeCalendarEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module/college-calendar")
@RequiredArgsConstructor
@Slf4j
public class CollegeCalendarEventController {

    private final CollegeCalendarEventService calendarService;

    /**
     * Get all college academic calendar events (unpaginated)
     */
    @GetMapping({"", "/getAll"})
    public ResponseEntity<StandardResponse<List<CollegeCalendarEventResponse>>> getAllEvents() {
        log.info("API call: GET /college-calendar/getAll");
        List<CollegeCalendarEventResponse> events = calendarService.getAllEvents();
        return ResponseEntity.ok(StandardResponse.success(events, "College calendar events retrieved successfully"));
    }

    /**
     * Filter calendar events unpaginated (for calendar month/year views)
     */
    @GetMapping("/list")
    public ResponseEntity<StandardResponse<List<CollegeCalendarEventResponse>>> listEvents(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String targetProgram,
            @RequestParam(required = false) String status) {
        log.info("API call: GET /college-calendar/list - year: {}, type: {}, sem: {}, prog: {}, status: {}",
                academicYear, eventType, semester, targetProgram, status);
        List<CollegeCalendarEventResponse> events = calendarService.listEvents(academicYear, eventType, semester, targetProgram, status);
        return ResponseEntity.ok(StandardResponse.success(events, "Filtered calendar events retrieved successfully"));
    }

    /**
     * Search and paginate calendar events
     */
    @GetMapping("/search")
    public ResponseEntity<StandardResponse<Page<CollegeCalendarEventResponse>>> searchEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String targetProgram,
            @RequestParam(required = false) String status) {
        log.info("API call: GET /college-calendar/search - page: {}, size: {}, search: {}", page, size, search);
        Page<CollegeCalendarEventResponse> pageResult = calendarService.searchEvents(page, size, search, academicYear, eventType, semester, targetProgram, status);
        return ResponseEntity.ok(StandardResponse.success(pageResult, "Calendar events paginated successfully"));
    }

    /**
     * Get a calendar event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<CollegeCalendarEventResponse>> getEventById(@PathVariable Long id) {
        log.info("API call: GET /college-calendar/{}", id);
        CollegeCalendarEventResponse event = calendarService.getEventById(id);
        return ResponseEntity.ok(StandardResponse.success(event, "Calendar event retrieved successfully"));
    }

    /**
     * Create a new college calendar event
     */
    @PostMapping("/create")
    public ResponseEntity<StandardResponse<CollegeCalendarEventResponse>> createEvent(
            @Valid @RequestBody CollegeCalendarEventRequest request) {
        log.info("API call: POST /college-calendar/create - title: {}, type: {}", request.getTitle(), request.getEventType());
        CollegeCalendarEventResponse created = calendarService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(created, "College calendar event scheduled successfully"));
    }

    /**
     * Update an existing calendar event
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<StandardResponse<CollegeCalendarEventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CollegeCalendarEventRequest request) {
        log.info("API call: PUT /college-calendar/update/{} - title: {}", id, request.getTitle());
        CollegeCalendarEventResponse updated = calendarService.updateEvent(id, request);
        return ResponseEntity.ok(StandardResponse.success(updated, "College calendar event updated successfully"));
    }

    /**
     * Soft delete a calendar event
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteEvent(@PathVariable Long id) {
        log.info("API call: DELETE /college-calendar/delete/{}", id);
        calendarService.deleteEvent(id);
        return ResponseEntity.ok(StandardResponse.success("College calendar event removed successfully"));
    }

    /**
     * Bulk create calendar events
     */
    @PostMapping("/bulkCreate")
    public ResponseEntity<StandardResponse<List<CollegeCalendarEventResponse>>> bulkCreate(
            @RequestBody List<CollegeCalendarEventRequest> requests) {
        log.info("API call: POST /college-calendar/bulkCreate - count: {}", requests != null ? requests.size() : 0);
        List<CollegeCalendarEventResponse> result = calendarService.bulkCreateEvents(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(result, "Bulk calendar events created successfully"));
    }
}
