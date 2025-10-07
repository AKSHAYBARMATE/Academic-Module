package com.academic.controller;

import com.academic.request.AcademicCalendarEventRequest;
import com.academic.response.AcademicCalendarEventResponse;
import com.academic.response.StandardResponse;
import com.academic.service.AcademicCalendarEventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module")
public class AcademicCalendarEventController {

    private final AcademicCalendarEventService service;

    public AcademicCalendarEventController(AcademicCalendarEventService service) {
        this.service = service;
    }

    // --- C - CREATE: POST /api/v1/academic-calendar/events ---
    @PostMapping("/createEvent")
    public ResponseEntity<StandardResponse<AcademicCalendarEventResponse>> create(@Valid @RequestBody AcademicCalendarEventRequest request) {
        AcademicCalendarEventResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(response, "Academic event created successfully"));
    }

    @GetMapping("/getAllEvents")
    public ResponseEntity<StandardResponse<List<AcademicCalendarEventResponse>>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AcademicCalendarEventResponse> response = service.findAll(search, type, status, date);
        return ResponseEntity.ok(StandardResponse.success(response, "Academic events fetched successfully"));
    }

    // --- R - READ BY ID: GET /api/v1/academic-calendar/events/{id} ---
    @GetMapping("/getEventById/{id}")
    public ResponseEntity<StandardResponse<AcademicCalendarEventResponse>> findById(@PathVariable Long id) {
        AcademicCalendarEventResponse response = service.findById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Academic event fetched successfully"));
    }

    // --- U - UPDATE: PUT /api/v1/academic-calendar/events/{id} ---
    @PutMapping("/updateEvent/{id}")
    public ResponseEntity<StandardResponse<AcademicCalendarEventResponse>> update(@PathVariable Long id, @Valid @RequestBody AcademicCalendarEventRequest request) {
        AcademicCalendarEventResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Academic event updated successfully"));
    }

    // --- D - DELETE (Soft Delete): DELETE /api/v1/academic-calendar/events/{id} ---
    @DeleteMapping("/deleteEvent/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        // Using StandardResponse<Void> for consistency, though 'data' will be null
        return ResponseEntity.ok(StandardResponse.success(null, "Academic event deleted successfully"));
    }
}
