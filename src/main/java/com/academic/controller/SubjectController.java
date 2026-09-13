package com.academic.controller;

import com.academic.request.SubjectRequest;
import com.academic.response.StandardResponse;
import com.academic.response.SubjectResponse;
import com.academic.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectService service;

    /**
     * Create a new School Subject record
     */
    @PostMapping("/createSubject")
    public ResponseEntity<StandardResponse<SubjectResponse>> create(@RequestBody SubjectRequest request) {
        log.info("API call: POST /createSubject - code: {}, name: {}",
                request != null ? request.getResolvedCode() : null,
                request != null ? request.getResolvedName() : null);
        SubjectResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(response, "Subject created successfully"));
    }

    /**
     * Update an existing School Subject record by ID
     */
    @PutMapping("/updateSubject/{id}")
    public ResponseEntity<StandardResponse<SubjectResponse>> update(
            @PathVariable Integer id,
            @RequestBody SubjectRequest request
    ) {
        log.info("API call: PUT /updateSubject/{} - payload: {}", id, request);
        SubjectResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject updated successfully"));
    }

    /**
     * Soft delete a School Subject record by ID
     */
    @DeleteMapping("/deleteSubject/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Integer id) {
        log.warn("API call: DELETE /deleteSubject/{}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Subject deleted successfully"));
    }

    /**
     * Get a single School Subject record by ID
     */
    @GetMapping("/getSubjectById/{id}")
    public ResponseEntity<StandardResponse<SubjectResponse>> getById(@PathVariable Integer id) {
        log.info("API call: GET /getSubjectById/{}", id);
        SubjectResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject fetched successfully"));
    }

    /**
     * List all active School Subjects
     */
    @GetMapping("/active")
    public ResponseEntity<StandardResponse<List<SubjectResponse>>> getActive() {
        log.info("API call: GET /active");
        List<SubjectResponse> response = service.getAllActive();
        return ResponseEntity.ok(StandardResponse.success(response, "Active subjects fetched successfully"));
    }

    /**
     * Filtered & paginated query across all School Subjects
     */
    @GetMapping("/getAllSubjects")
    public ResponseEntity<StandardResponse<Page<SubjectResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer credits
    ) {
        log.info("API call: GET /getAllSubjects - page: {}, size: {}, search: {}, dept: {}, type: {}, status: {}",
                page, size, search, department, type, status);
        Page<SubjectResponse> response = service.getAll(
                page, size, search, department, type, status, credits
        );
        return ResponseEntity.ok(StandardResponse.success(response, "Subjects fetched successfully"));
    }
}
