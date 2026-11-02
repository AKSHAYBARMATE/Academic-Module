package com.academic.controller;

import com.academic.dto.exam.*;
import com.academic.response.StandardResponse;
import com.academic.service.AcademicExamSchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for:
 * 1. Screen 3: Exam Setup & Scheduling (Exam Setup, Subject & Marks Matrix, Exam Schedule Slots)
 * 2. Screen 4: Marks Entry & Result Processing (Load Grid, Save Draft, Validate, Submit, Lock, Unlock, Export Excel)
 * Does not touch existing legacy exam tables.
 */
@RestController
@RequestMapping("/api/v1/academic-module")
@RequiredArgsConstructor
public class AcademicExamSchedulingController {

    private final AcademicExamSchedulingService examSchedulingService;

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 3: Step 1 – Exam Setup
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/exam-management/saveExamSetup")
    public ResponseEntity<StandardResponse<AcademicExamResponse>> saveExamSetup(
            @Valid @RequestBody AcademicExamRequest request) {
        StandardResponse<AcademicExamResponse> response = examSchedulingService.saveExamSetup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/exam-management/getExamSetup/{id}")
    public ResponseEntity<StandardResponse<AcademicExamResponse>> getExamSetup(@PathVariable Long id) {
        return ResponseEntity.ok(examSchedulingService.getExamSetup(id));
    }

    @GetMapping("/exam-management/listExams")
    public ResponseEntity<StandardResponse<Page<AcademicExamResponse>>> listExams(
            @RequestParam(required = false) String academicSession,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(examSchedulingService.listExams(academicSession, status, search, page, size));
    }

    @PatchMapping("/exam-management/{id}/updateStatus")
    public ResponseEntity<StandardResponse<AcademicExamResponse>> updateExamStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(examSchedulingService.updateExamStatus(id, status));
    }

    @DeleteMapping("/exam-management/{id}")
    public ResponseEntity<StandardResponse<Map<String, Object>>> deleteExam(@PathVariable Long id) {
        return ResponseEntity.ok(examSchedulingService.deleteExam(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 3: Step 2 – Subject & Marks Matrix
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/exam-management/saveSubjectMatrix")
    public ResponseEntity<StandardResponse<SubjectMarksMatrixResponse>> saveSubjectMatrix(
            @Valid @RequestBody SubjectMarksMatrixRequest request) {
        return ResponseEntity.ok(examSchedulingService.saveSubjectMatrix(request));
    }

    @GetMapping("/exam-management/getSubjectMatrix/{examId}")
    public ResponseEntity<StandardResponse<SubjectMarksMatrixResponse>> getSubjectMatrix(
            @PathVariable Long examId,
            @RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(examSchedulingService.getSubjectMatrix(examId, classId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 3: Step 3 – Exam Schedule Slots
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/exam-management/saveScheduleSlots")
    public ResponseEntity<StandardResponse<ExamScheduleResponse>> saveScheduleSlots(
            @Valid @RequestBody ExamScheduleRequest request) {
        return ResponseEntity.ok(examSchedulingService.saveScheduleSlots(request));
    }

    @GetMapping("/exam-management/getScheduleSlots")
    public ResponseEntity<StandardResponse<ExamScheduleResponse>> getScheduleSlots(
            @RequestParam Long examId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId) {
        return ResponseEntity.ok(examSchedulingService.getScheduleSlots(examId, classId, sectionId));
    }

    @DeleteMapping("/exam-management/scheduleSlot/{slotId}")
    public ResponseEntity<StandardResponse<Map<String, Object>>> deleteScheduleSlot(@PathVariable Long slotId) {
        return ResponseEntity.ok(examSchedulingService.deleteScheduleSlot(slotId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Screen 4: Marks Entry & Result Processing
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/exam-marks/loadMarksGrid")
    public ResponseEntity<StandardResponse<MarksGridLoadResponse>> loadMarksGrid(
            @RequestParam Long examId,
            @RequestParam Long classId,
            @RequestParam Long sectionId,
            @RequestParam Long subjectId,
            @RequestParam(required = false) String assessmentComponent) {
        return ResponseEntity.ok(examSchedulingService.loadMarksGrid(examId, classId, sectionId, subjectId, assessmentComponent));
    }

    @PostMapping("/exam-marks/saveDraft")
    public ResponseEntity<StandardResponse<MarksGridLoadResponse>> saveDraft(
            @Valid @RequestBody MarksEntryBatchRequest request) {
        return ResponseEntity.ok(examSchedulingService.saveMarksDraft(request));
    }

    @PostMapping("/exam-marks/validateMarks")
    public ResponseEntity<StandardResponse<MarksValidationResponse>> validateMarks(
            @Valid @RequestBody MarksEntryBatchRequest request) {
        return ResponseEntity.ok(examSchedulingService.validateMarks(request));
    }

    @PostMapping("/exam-marks/submitMarks")
    public ResponseEntity<StandardResponse<MarksGridLoadResponse>> submitMarks(
            @Valid @RequestBody MarksEntryBatchRequest request) {
        return ResponseEntity.ok(examSchedulingService.submitMarks(request));
    }

    @PostMapping("/exam-marks/lockMarks")
    public ResponseEntity<StandardResponse<Map<String, Object>>> lockMarks(
            @RequestParam Long examId,
            @RequestParam Long classId,
            @RequestParam Long sectionId,
            @RequestParam Long subjectId) {
        return ResponseEntity.ok(examSchedulingService.lockMarks(examId, classId, sectionId, subjectId));
    }

    @PostMapping("/exam-marks/unlockMarks")
    public ResponseEntity<StandardResponse<Map<String, Object>>> unlockMarks(
            @Valid @RequestBody MarksUnlockRequest request) {
        return ResponseEntity.ok(examSchedulingService.unlockMarks(request));
    }

    @GetMapping("/exam-marks/exportExcel")
    public ResponseEntity<byte[]> exportMarksExcel(
            @RequestParam Long examId,
            @RequestParam Long classId,
            @RequestParam Long sectionId,
            @RequestParam Long subjectId) {
        byte[] bytes = examSchedulingService.exportMarksExcel(examId, classId, sectionId, subjectId);
        String filename = "MarksEntry_Exam" + examId + "_Class" + classId + "_Sec" + sectionId + "_Sub" + subjectId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}

