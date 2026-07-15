package com.academic.controller;

import com.academic.request.ProxyAssignmentRequest;
import com.academic.request.ProxyTemplateRequest;
import com.academic.request.TimeSlotDTO;
import com.academic.request.TimeTableRequest;
import com.academic.response.LogContext;
import com.academic.response.ProxyAssignmentResponse;
import com.academic.response.ProxyTemplateResponse;
import com.academic.response.StandardResponse;
import com.academic.response.TimeTableResponse;
import com.academic.service.ProxyService;
import com.academic.service.TimeTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/academic-module")
@RequiredArgsConstructor
@Slf4j
public class TimeTableController {

    private final TimeTableService service;
    private final ProxyService proxyService;

    /**
     * Create a new timetable
     */
    @PostMapping("/createTimetable")
    public ResponseEntity<StandardResponse<TimeTableResponse>> create(
            @Validated @RequestBody TimeTableRequest request) {
        log.info("[{}][{}] API - Create Timetable: {}", LogContext.getRequestId(), LogContext.getLogId(),
                request.getTimetableName());
        StandardResponse<TimeTableResponse> response = service.create(request);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<StandardResponse<TimeTableResponse>> update(@PathVariable Long id,
            @Validated @RequestBody TimeTableRequest request) {
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
     * @param page    Page number (1-based)
     * @param size    Page size
     * @param classId Optional class filter
     * @param section Optional section filter
     * @param search  Optional search on timetable name
     */
    @GetMapping("/listAllTimetables")
    public ResponseEntity<StandardResponse<Map<String, Object>>> listAll(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "section", required = false) Long section,
            @RequestParam(value = "search", required = false) String search) {
        log.info("[{}][{}] API - List Timetables: page={}, size={}, classId={}, section={}, search={}",
                LogContext.getRequestId(), LogContext.getLogId(),
                page, size, classId, section, search);

        // Call service which returns StandardResponse<Map<String, Object>>
        StandardResponse<Map<String, Object>> response = service.listAll(page, size, classId, section, search);

        return ResponseEntity.ok(response);
    }

    /**
     * Download the timetable PDF by ID
     */
    @GetMapping("/downloadTimetablePdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        log.info("[{}][{}] API - Download Timetable PDF ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        byte[] pdf = service.generateTimetablePdf(id);
        String fileName = "timetable_" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // =========================================================================
    // PROXY TEMPLATES APIs
    // =========================================================================

    /**
     * Create a new substitute proxy template defining the cover context and
     * stand-in staff.
     */
    @PostMapping("/createProxyTemplate")
    public ResponseEntity<StandardResponse<ProxyTemplateResponse>> createTemplate(
            @Validated @RequestBody ProxyTemplateRequest request) {
        log.info("[{}][{}] API - Create Proxy Template: {}",
                LogContext.getRequestId(), LogContext.getLogId(), request.getTemplateName());
        return ResponseEntity.ok(proxyService.createTemplate(request));
    }

    /**
     * Update an existing proxy template.
     */
    @PutMapping("/updateProxyTemplate/{id}")
    public ResponseEntity<StandardResponse<ProxyTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Validated @RequestBody ProxyTemplateRequest request) {
        log.info("[{}][{}] API - Update Proxy Template ID: {}",
                LogContext.getRequestId(), LogContext.getLogId(), id);
        return ResponseEntity.ok(proxyService.updateTemplate(id, request));
    }

    /**
     * Fetch a single proxy template by ID.
     */
    @GetMapping("/getProxyTemplateById/{id}")
    public ResponseEntity<StandardResponse<ProxyTemplateResponse>> getTemplateById(@PathVariable Long id) {
        log.info("[{}][{}] API - Get Proxy Template ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        return ResponseEntity.ok(proxyService.getTemplateById(id));
    }

    /**
     * Soft-delete a proxy template.
     */
    @DeleteMapping("/deleteProxyTemplate/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("[{}][{}] API - Delete Proxy Template ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        return ResponseEntity.ok(proxyService.deleteTemplate(id));
    }

    /**
     * List all proxy templates (paginated, with search on template name / teacher
     * name).
     */
    @GetMapping("/listProxyTemplates")
    public ResponseEntity<StandardResponse<Map<String, Object>>> listTemplates(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "search", required = false) String search) {
        log.info("[{}][{}] API - List Proxy Templates: page={}, size={}, search={}",
                LogContext.getRequestId(), LogContext.getLogId(), page, size, search);
        return ResponseEntity.ok(proxyService.listTemplates(page, size, search));
    }

    // =========================================================================
    // PROXY ASSIGNMENT / LOGS APIs
    // =========================================================================

    /**
     * Schedule a one-time proxy slot coverage from a template for a timetable
     * block.
     */
    @PostMapping("/createProxyAssignment")
    public ResponseEntity<StandardResponse<ProxyAssignmentResponse>> assignProxySlot(
            @Validated @RequestBody ProxyAssignmentRequest request) {
        log.info("[{}][{}] API - Assign Proxy Slot: templateId={}, slotId={}, date={}",
                LogContext.getRequestId(), LogContext.getLogId(),
                request.getTemplateId(), request.getSlotId(), request.getProxyDate());
        return ResponseEntity.ok(proxyService.assignProxySlot(request));
    }

    /**
     * Soft-delete/remove a scheduled proxy assignment.
     */
    @DeleteMapping("/deleteProxyAssignment/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteAssignment(@PathVariable Long id) {
        log.info("[{}][{}] API - Delete Proxy Assignment ID: {}", LogContext.getRequestId(), LogContext.getLogId(), id);
        return ResponseEntity.ok(proxyService.deleteAssignment(id));
    }

    /**
     * List scheduled proxy assignments (logs) with pagination and filters.
     */
    @GetMapping("/listProxyAssignments")
    public ResponseEntity<StandardResponse<Map<String, Object>>> listAssignments(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "templateId", required = false) Long templateId,
            @RequestParam(value = "timetableId", required = false) Long timetableId,
            @RequestParam(value = "proxyDate", required = false) String proxyDate,
            @RequestParam(value = "status", required = false) String status) {
        log.info(
                "[{}][{}] API - List Proxy Assignments: page={}, size={}, templateId={}, timetableId={}, date={}, status={}",
                LogContext.getRequestId(), LogContext.getLogId(), page, size, templateId, timetableId, proxyDate,
                status);
        return ResponseEntity.ok(proxyService.listAssignments(page, size, templateId, timetableId, proxyDate, status));
    }

    /**
     * Get all proxy assignments scheduled for a specific teacher.
     */
    @GetMapping("/getProxyAssignmentsForTeacher/{teacherId}")
    public ResponseEntity<StandardResponse<List<ProxyAssignmentResponse>>> getAssignmentsForTeacher(
            @PathVariable Long teacherId) {
        log.info("[{}][{}] API - Get Assignments for Teacher ID: {}", LogContext.getRequestId(), LogContext.getLogId(),
                teacherId);
        return ResponseEntity.ok(proxyService.getAssignmentsForTeacher(teacherId));
    }

    /**
     * Get available timetable slot time blocks search for a class, section and
     * dayOfWeek
     * (used in the Modal slider screen to select slots).
     */
    @GetMapping("/getAvailableTimetableSlots")
    public ResponseEntity<StandardResponse<List<TimeSlotDTO>>> getAvailableTimetableSlots(
            @RequestParam("classId") Long classId,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam("dayOfWeek") Integer dayOfWeek) {
        log.info("[{}][{}] API - Available Timetable Slots: classId={}, sectionId={}, dayOfWeek={}",
                LogContext.getRequestId(), LogContext.getLogId(), classId, sectionId, dayOfWeek);
        return ResponseEntity.ok(proxyService.getAvailableTimetableSlots(classId, sectionId, dayOfWeek));
    }

}
