package com.academic.controller;

import com.academic.request.ExamSetupRequest;
import com.academic.response.ExamSetupResponse;
import com.academic.response.StandardResponse;
import com.academic.service.ExamSetupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sms/api/v1/academic-module")
@RequiredArgsConstructor
public class ExamSetupController {

    private final ExamSetupService service;

    @PostMapping("/createExamSetup")
    public ResponseEntity<StandardResponse<ExamSetupResponse>> create(@RequestBody ExamSetupRequest request) {
        log.info("API - Create ExamSetup: {}", request);
        ExamSetupResponse created = service.create(request);
        return ResponseEntity.ok(StandardResponse.success(created, "Exam setup created successfully"));
    }

    @PutMapping("/updateExamSetup/{id}")
    public ResponseEntity<StandardResponse<ExamSetupResponse>> update(@PathVariable Long id, @RequestBody ExamSetupRequest request) {
        log.info("API - Update ExamSetup ID: {}", id);
        ExamSetupResponse updated = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(updated, "Exam setup updated successfully"));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<StandardResponse<ExamSetupResponse>> getById(@PathVariable Long id) {
        log.info("API - Get ExamSetup ID: {}", id);
        ExamSetupResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Exam setup fetched successfully"));
    }

    @GetMapping("/getAllExamSetups")
    public ResponseEntity<StandardResponse<List<ExamSetupResponse>>> getAll(
            @RequestParam(value = "examName", required = false) String examName,
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "academicId", required = false) Long academicId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        log.info("API - Get All ExamSetups with filters - examName: {}, classId: {}, academicId: {}, page: {}, size: {}",
                examName, classId, academicId, page, size);

        List<ExamSetupResponse> list = service.getAllFiltered(examName, classId, academicId, page, size)
                .getContent();

        return ResponseEntity.ok(
                StandardResponse.success(list, "Exam setups fetched successfully")
        );
    }

    @DeleteMapping("/deleteExamSetup/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        log.info("API - Soft Delete ExamSetup ID: {}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Exam setup deleted successfully"));
    }
}
