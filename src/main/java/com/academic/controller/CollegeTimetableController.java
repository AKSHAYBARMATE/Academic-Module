package com.academic.controller;

import com.academic.request.CollegeTimetableRequest;
import com.academic.response.CollegeTimetableResponse;
import com.academic.response.StandardResponse;
import com.academic.service.CollegeTimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module/college-timetable")
@RequiredArgsConstructor
@Slf4j
public class CollegeTimetableController {

    private final CollegeTimetableService collegeTimetableService;

    /**
     * Create a single college timetable slot
     */
    @PostMapping("/createSlot")
    public ResponseEntity<StandardResponse<CollegeTimetableResponse>> createSlot(
            @Valid @RequestBody CollegeTimetableRequest request) {
        log.info("API call: POST /college-timetable/createSlot - program: {}, sem: {}, sec: {}, day: {}, slot: {}, subjectId: {}, staffId: {}",
                request.getProgram(), request.getSemester(), request.getSection(),
                request.getDay(), request.getTimeSlot(), request.getSubjectId(), request.getStaffId());
        CollegeTimetableResponse response = collegeTimetableService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(response, "College timetable slot created successfully"));
    }

    /**
     * Update an existing college timetable slot
     */
    @PutMapping("/updateSlot/{id}")
    public ResponseEntity<StandardResponse<CollegeTimetableResponse>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody CollegeTimetableRequest request) {
        log.info("API call: PUT /college-timetable/updateSlot/{} - program: {}, sem: {}, sec: {}, day: {}, slot: {}",
                id, request.getProgram(), request.getSemester(), request.getSection(), request.getDay(), request.getTimeSlot());
        CollegeTimetableResponse response = collegeTimetableService.updateSlot(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "College timetable slot updated successfully"));
    }

    /**
     * Soft delete a timetable slot
     */
    @DeleteMapping("/deleteSlot/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteSlot(@PathVariable Long id) {
        log.warn("API call: DELETE /college-timetable/deleteSlot/{}", id);
        collegeTimetableService.deleteSlot(id);
        return ResponseEntity.ok(StandardResponse.success("College timetable slot deleted successfully"));
    }

    /**
     * Get a single timetable slot by ID
     */
    @GetMapping("/getSlot/{id}")
    public ResponseEntity<StandardResponse<CollegeTimetableResponse>> getSlotById(@PathVariable Long id) {
        log.info("API call: GET /college-timetable/getSlot/{}", id);
        CollegeTimetableResponse response = collegeTimetableService.getSlotById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "College timetable slot retrieved successfully"));
    }

    /**
     * Get weekly class schedule grid for a section
     */
    @GetMapping("/getClassSchedule")
    public ResponseEntity<StandardResponse<List<CollegeTimetableResponse>>> getClassSchedule(
            @RequestParam String program,
            @RequestParam String semester,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String academicYear) {
        log.info("API call: GET /college-timetable/getClassSchedule - program: {}, sem: {}, sec: {}, year: {}",
                program, semester, section, academicYear);
        List<CollegeTimetableResponse> response = collegeTimetableService.getClassSchedule(program, semester, section, academicYear);
        return ResponseEntity.ok(StandardResponse.success(response, "Class schedule retrieved successfully"));
    }

    /**
     * Get weekly teacher schedule
     */
    @GetMapping("/getTeacherSchedule")
    public ResponseEntity<StandardResponse<List<CollegeTimetableResponse>>> getTeacherSchedule(
            @RequestParam Integer staffId,
            @RequestParam(required = false) String academicYear) {
        log.info("API call: GET /college-timetable/getTeacherSchedule - staffId: {}, year: {}", staffId, academicYear);
        List<CollegeTimetableResponse> response = collegeTimetableService.getTeacherSchedule(staffId, academicYear);
        return ResponseEntity.ok(StandardResponse.success(response, "Teacher schedule retrieved successfully"));
    }

    /**
     * Bulk save / update slots (e.g. for grid editor or publish)
     */
    @PostMapping("/bulkSave")
    public ResponseEntity<StandardResponse<List<CollegeTimetableResponse>>> bulkSave(
            @Valid @RequestBody List<CollegeTimetableRequest> requests) {
        log.info("API call: POST /college-timetable/bulkSave - slot count: {}", requests != null ? requests.size() : 0);
        List<CollegeTimetableResponse> response = collegeTimetableService.bulkSaveSchedule(requests);
        return ResponseEntity.ok(StandardResponse.success(response, "Timetable slots saved successfully"));
    }

    /**
     * Search and paginate all college timetable slots
     */
    @GetMapping("/getAllSlots")
    public ResponseEntity<StandardResponse<Page<CollegeTimetableResponse>>> getAllSlots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer staffId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        log.info("API call: GET /college-timetable/getAllSlots - page: {}, size: {}, search: {}, program: {}, sem: {}, sec: {}, day: {}, year: {}, staffId: {}, subjectId: {}",
                page, size, search, program, semester, section, day, academicYear, staffId, subjectId);
        Page<CollegeTimetableResponse> response = collegeTimetableService.getAllSlots(
                page, size, search, program, semester, section, day, academicYear, staffId, subjectId, type, status);
        return ResponseEntity.ok(StandardResponse.success(response, "Timetable slots retrieved successfully"));
    }
}
