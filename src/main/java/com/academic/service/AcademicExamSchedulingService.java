package com.academic.service;

import com.academic.dto.exam.*;
import com.academic.response.StandardResponse;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface AcademicExamSchedulingService {

    // ── Screen 3: Step 1 – Exam Setup ──────────────────────────────────────────
    StandardResponse<AcademicExamResponse> saveExamSetup(AcademicExamRequest request);

    StandardResponse<AcademicExamResponse> getExamSetup(Long id);

    StandardResponse<Page<AcademicExamResponse>> listExams(String academicSession, String status, String search, int page, int size);

    StandardResponse<AcademicExamResponse> updateExamStatus(Long id, String status);

    StandardResponse<Map<String, Object>> deleteExam(Long id);

    // ── Screen 3: Step 2 – Subject & Marks Matrix ──────────────────────────────
    StandardResponse<SubjectMarksMatrixResponse> saveSubjectMatrix(SubjectMarksMatrixRequest request);

    StandardResponse<SubjectMarksMatrixResponse> getSubjectMatrix(Long examId, Long classId);

    // ── Screen 3: Step 3 – Exam Schedule Slots ────────────────────────────────
    StandardResponse<ExamScheduleResponse> saveScheduleSlots(ExamScheduleRequest request);

    StandardResponse<ExamScheduleResponse> getScheduleSlots(Long examId, Long classId, Long sectionId);

    StandardResponse<Map<String, Object>> deleteScheduleSlot(Long slotId);

    // ── Screen 4: Marks Entry & Result Processing ──────────────────────────────
    StandardResponse<MarksGridLoadResponse> loadMarksGrid(Long examId, Long classId, Long sectionId, Long subjectId, String assessmentComponent);

    StandardResponse<MarksGridLoadResponse> saveMarksDraft(MarksEntryBatchRequest request);

    StandardResponse<MarksValidationResponse> validateMarks(MarksEntryBatchRequest request);

    StandardResponse<MarksGridLoadResponse> submitMarks(MarksEntryBatchRequest request);

    StandardResponse<Map<String, Object>> lockMarks(Long examId, Long classId, Long sectionId, Long subjectId);

    StandardResponse<Map<String, Object>> unlockMarks(MarksUnlockRequest request);

    byte[] exportMarksExcel(Long examId, Long classId, Long sectionId, Long subjectId);
}

