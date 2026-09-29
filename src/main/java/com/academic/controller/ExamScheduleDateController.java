package com.academic.controller;

import com.academic.request.ExamScheduleDateRequest;
import com.academic.response.StandardResponse;
import com.academic.service.ExamScheduleDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/academic-module/exam-schedule-dates")
@RequiredArgsConstructor
public class ExamScheduleDateController {

    private final ExamScheduleDateService service;

    /**
     * Save or update a single exam schedule date
     */
    @PostMapping("/saveExamScheduleDate")
    public ResponseEntity<StandardResponse<?>> save(@RequestBody ExamScheduleDateRequest request) {
        log.info("API - Save Exam Schedule Date: {}", request);
        return ResponseEntity.ok(service.save(request));
    }

    /**
     * Bulk save exam schedule dates
     */
    @PostMapping("/bulkSaveExamScheduleDates")
    public ResponseEntity<StandardResponse<?>> bulkSave(@RequestBody List<ExamScheduleDateRequest> requests) {
        log.info("API - Bulk Save Exam Schedule Dates count: {}", requests != null ? requests.size() : 0);
        return ResponseEntity.ok(service.bulkSave(requests));
    }

    /**
     * Get single exam schedule date by ID
     */
    @GetMapping("/getExamScheduleDate/{id}")
    public ResponseEntity<StandardResponse<?>> getById(@PathVariable Long id) {
        log.info("API - Get Exam Schedule Date ID: {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Get all exam schedule dates with filters (Class-wise, Date-wise, Session, Section, Exam Schedule)
     */
    @GetMapping("/getAllExamScheduleDates")
    public ResponseEntity<StandardResponse<?>> getAll(
            @RequestParam(required = false) Long examScheduleId,
            @RequestParam(required = false) Integer sessionId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("API - Get All Exam Schedule Dates: examScheduleId={}, sessionId={}, classId={}, sectionId={}, examDate={}, startDate={}, endDate={}",
                examScheduleId, sessionId, classId, sectionId, examDate, startDate, endDate);
        return ResponseEntity.ok(service.getAll(examScheduleId, sessionId, classId, sectionId, examDate, startDate, endDate));
    }

    /**
     * Delete an exam schedule date
     */
    @DeleteMapping("/deleteExamScheduleDate/{id}")
    public ResponseEntity<StandardResponse<?>> delete(@PathVariable Long id) {
        log.info("API - Delete Exam Schedule Date ID: {}", id);
        return ResponseEntity.ok(service.delete(id));
    }

    /**
     * Download Exam Timetable PDF with Class-wise and Date-wise filters
     */
    @GetMapping("/downloadExamTimeTablePdf")
    public ResponseEntity<byte[]> downloadExamTimeTablePdf(
            @RequestParam(required = false) Long examScheduleId,
            @RequestParam(required = false) Integer sessionId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("API - Download Exam Timetable PDF: examScheduleId={}, sessionId={}, classId={}, sectionId={}, examDate={}, startDate={}, endDate={}",
                examScheduleId, sessionId, classId, sectionId, examDate, startDate, endDate);

        byte[] pdfBytes = service.generateExamTimeTablePdf(
                examScheduleId, sessionId, classId, sectionId, examDate, startDate, endDate
        );

        String fileName = "Exam_TimeTable"
                + (classId != null ? "_Class_" + classId : "")
                + (examDate != null ? "_" + examDate : "")
                + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
