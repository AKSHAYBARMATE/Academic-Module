package com.academic.controller;

import com.academic.request.TimeTableRequest;
import com.academic.response.LogContext;
import com.academic.response.StandardResponse;
import com.academic.response.TimeTableResponse;
import com.academic.service.TimeTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sms/api/v1/academic-module")
@RequiredArgsConstructor
@Slf4j
public class TimeTableController {

    private final TimeTableService service;

    /**
     * Create a new timetable
     */
    @PostMapping("/createTimetable")
    public ResponseEntity<StandardResponse<TimeTableResponse>> create(@Validated @RequestBody TimeTableRequest request) {
        log.info("[{}][{}] API - Create Timetable: {}", LogContext.getRequestId(), LogContext.getLogId(), request.getTimetableName());
        TimeTableResponse response = service.create(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Timetable created successfully"));
    }

    /**
     * Get a single timetable by ID
     */
    @GetMapping("/getTimetableById/{id}")
    public ResponseEntity<StandardResponse<TimeTableResponse>> get(@PathVariable Long id) {
        log.info("[{}][{}] API - Fetch Timetable by ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        TimeTableResponse response = service.get(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Timetable fetched successfully"));
    }

    /**
     * Update an existing timetable
     */
    @PutMapping("/updateTimeTableById/{id}")
    public ResponseEntity<StandardResponse<TimeTableResponse>> update(@PathVariable Long id, @Validated @RequestBody TimeTableRequest request) {
        log.info("[{}][{}] API - Update Timetable ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        TimeTableResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Timetable updated successfully"));
    }

    /**
     * Soft delete a timetable by ID
     */
    @DeleteMapping("/deleteTimeTableById/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        log.info("[{}][{}] API - Soft Delete Timetable ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Timetable deleted successfully"));
    }

    /**
     * List all timetables with pagination, optional filters, and search
     *
     * @param page     Page number (1-based)
     * @param size     Page size
     * @param classId  Optional class filter
     * @param section  Optional section filter
     * @param search   Optional search on timetable name
     */
    @GetMapping("/listAllTimetables")
    public ResponseEntity<StandardResponse<Map<String, Object>>> listAll(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "section", required = false) Long section,
            @RequestParam(value = "search", required = false) String search
    ) {
        log.info("[{}][{}] API - List Timetables: page={}, size={}, classId={}, section={}, search={}",
                LogContext.getRequestId(), LogContext.getLogId(),
                page, size, classId, section, search);

        // Call service which returns StandardResponse<Map<String, Object>>
        StandardResponse<Map<String, Object>> response = service.listAll(page, size, classId, section, search);

        return ResponseEntity.ok(response);
    }

}
