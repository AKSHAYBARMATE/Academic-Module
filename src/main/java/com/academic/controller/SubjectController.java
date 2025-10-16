package com.academic.controller;


import com.academic.request.SubjectRequest;
import com.academic.response.StandardResponse;
import com.academic.response.SubjectResponse;
import com.academic.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sms/api/v1/academic-module")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectService service;

    @PostMapping("/createSubject")
    public ResponseEntity<StandardResponse<SubjectResponse>> create(@RequestBody SubjectRequest request) {
        SubjectResponse response = service.create(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject created successfully"));
    }

    @PutMapping("updateSubject/{id}")
    public ResponseEntity<StandardResponse<SubjectResponse>> update(@PathVariable Long id, @RequestBody SubjectRequest request) {
        SubjectResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject updated successfully"));
    }

    @DeleteMapping("/deleteSubject/{id}")
    public ResponseEntity<StandardResponse<Map<String, Object>>> delete(@PathVariable Long id) {
        service.delete(id); // perform the deletion
        return ResponseEntity.ok(
                StandardResponse.<Void>success(null, "Subject deleted successfully")
        );
    }


    @GetMapping("/getSubjectById/{id}")
    public ResponseEntity<StandardResponse<SubjectResponse>> getById(@PathVariable Long id) {
        SubjectResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject fetched successfully"));
    }

    @GetMapping("/getAllSubjects")
    public ResponseEntity<StandardResponse<Page<SubjectResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer credits) {

        Page<SubjectResponse> response = service.getAll(page, size, search, type, status, credits);
        return ResponseEntity.ok(StandardResponse.success(response, "Subjects fetched successfully"));
    }
}
