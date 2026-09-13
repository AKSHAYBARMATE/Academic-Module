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
     * Create a new Subject / Course record
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
     * Update an existing Subject / Course record by ID
     */
    @PutMapping("/updateSubject/{id}")
    public ResponseEntity<StandardResponse<SubjectResponse>> update(
            @PathVariable Long id,
            @RequestBody SubjectRequest request
    ) {
        log.info("API call: PUT /updateSubject/{} - payload: {}", id, request);
        SubjectResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject updated successfully"));
    }

    /**
     * Soft delete a Subject / Course record by ID
     */
    @DeleteMapping("/deleteSubject/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        log.warn("API call: DELETE /deleteSubject/{}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Subject deleted successfully"));
    }

    /**
     * Fetch Subject / Course details by ID
     */
    @GetMapping("/getSubjectById/{id}")
    public ResponseEntity<StandardResponse<SubjectResponse>> getById(@PathVariable Long id) {
        log.info("API call: GET /getSubjectById/{}", id);
        SubjectResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Subject fetched successfully"));
    }

    /**
     * Fetch all active subjects for dropdown selectors
     */
    @GetMapping("/getActiveSubjects")
    public ResponseEntity<StandardResponse<List<SubjectResponse>>> getActiveSubjects() {
        log.info("API call: GET /getActiveSubjects");
        List<SubjectResponse> response = service.getAllActive();
        return ResponseEntity.ok(StandardResponse.success(response, "Active subjects fetched successfully"));
    }

    /**
     * Fetch subjects mapped under a specific Program (e.g. "B.Tech (CSE)")
     */
    @GetMapping("/getSubjectsByProgram/{program}")
    public ResponseEntity<StandardResponse<List<SubjectResponse>>> getSubjectsByProgram(
            @PathVariable String program
    ) {
        log.info("API call: GET /getSubjectsByProgram/{}", program);
        List<SubjectResponse> response = service.getByProgram(program);
        return ResponseEntity.ok(StandardResponse.success(response, "Subjects for program fetched successfully"));
    }

    /**
     * Fetch subjects under a specific Department
     */
    @GetMapping("/getSubjectsByDepartment/{department}")
    public ResponseEntity<StandardResponse<List<SubjectResponse>>> getSubjectsByDepartment(
            @PathVariable String department
    ) {
        log.info("API call: GET /getSubjectsByDepartment/{}", department);
        List<SubjectResponse> response = service.getByDepartment(department);
        return ResponseEntity.ok(StandardResponse.success(response, "Subjects for department fetched successfully"));
    }

    /**
     * Fetch paginated and filtered list of Subjects / Courses
     */
    @GetMapping("/getAllSubjects")
    public ResponseEntity<StandardResponse<Page<SubjectResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer credits
    ) {
        log.info("API call: GET /getAllSubjects - page: {}, size: {}, search: {}, dept: {}, prog: {}, sem: {}, year: {}, type: {}, status: {}",
                page, size, search, department, program, semester, academicYear, type, status);
        Page<SubjectResponse> response = service.getAll(
                page, size, search, department, program, semester, academicYear, type, status, credits
        );
        return ResponseEntity.ok(StandardResponse.success(response, "Subjects fetched successfully"));
    }
}
