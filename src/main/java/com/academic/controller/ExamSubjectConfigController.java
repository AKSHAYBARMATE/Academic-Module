package com.academic.controller;

import com.academic.request.ExamSubjectConfigBulkRequest;
import com.academic.request.ExamSubjectMarksBulkUpdateRequest;
import com.academic.response.StandardResponse;
import com.academic.service.ExamSubjectConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module")
@RequiredArgsConstructor
public class ExamSubjectConfigController {

    private final ExamSubjectConfigService service;

    /* BULK CREATE */
    @PostMapping("/saveExamSubjectMarks")
    public StandardResponse<?> createBulk(
            @RequestBody ExamSubjectConfigBulkRequest request) {
        return service.createBulk(request);
    }

    /* READ */
    @GetMapping("/getAllExamSubjectMarks")
    public StandardResponse<?> getAll(
            @RequestParam Integer sessionId,
            @RequestParam(required = false) Integer examTypeId,
            @RequestParam(required = false) Integer classId) {
        return service.getAll(sessionId, examTypeId, classId);
    }

    /* UPDATE */
    @PutMapping("/updateExamSubjectMarksBulk")
    public StandardResponse<?> updateBulk(
            @RequestBody ExamSubjectMarksBulkUpdateRequest request) {
        return service.updateBulk(request);
    }

    /* DELETE */
    @DeleteMapping("deleteExamSubjectMarks/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
