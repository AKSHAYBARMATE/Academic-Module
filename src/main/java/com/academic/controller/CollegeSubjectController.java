package com.academic.controller;

import com.academic.entity.Department;
import com.academic.repository.DepartmentRepository;
import com.academic.request.CollegeSubjectRequest;
import com.academic.response.CollegeSubjectResponse;
import com.academic.response.StandardResponse;
import com.academic.service.CollegeSubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module/college-subject")
@RequiredArgsConstructor
@Slf4j
public class CollegeSubjectController {

    private final CollegeSubjectService service;
    private final DepartmentRepository departmentRepository;

    /**
     * Create a new College Subject / Course record
     */
    @PostMapping("/createSubject")
    public ResponseEntity<StandardResponse<CollegeSubjectResponse>> create(@RequestBody CollegeSubjectRequest request) {
        log.info("API call: POST /college-subject/createSubject - code: {}, name: {}, degreeId: {}, deptId: {}",
                request != null ? request.getResolvedCode() : null,
                request != null ? request.getResolvedName() : null,
                request != null ? request.getDegreeId() : null,
                request != null ? request.getDepartmentId() : null);
        CollegeSubjectResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(response, "College subject created successfully"));
    }

    /**
     * Update an existing College Subject / Course record by ID
     */
    @PutMapping("/updateSubject/{id}")
    public ResponseEntity<StandardResponse<CollegeSubjectResponse>> update(
            @PathVariable Long id,
            @RequestBody CollegeSubjectRequest request
    ) {
        log.info("API call: PUT /college-subject/updateSubject/{} - payload: {}", id, request);
        CollegeSubjectResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "College subject updated successfully"));
    }

    /**
     * Soft delete a College Subject / Course record by ID
     */
    @DeleteMapping("/deleteSubject/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        log.warn("API call: DELETE /college-subject/deleteSubject/{}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("College subject deleted successfully"));
    }

    /**
     * Get a single College Subject / Course record by ID
     */
    @GetMapping("/getSubjectById/{id}")
    public ResponseEntity<StandardResponse<CollegeSubjectResponse>> getById(@PathVariable Long id) {
        log.info("API call: GET /college-subject/getSubjectById/{}", id);
        CollegeSubjectResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "College subject fetched successfully"));
    }

    /**
     * List all active College Subjects
     */
    @GetMapping("/active")
    public ResponseEntity<StandardResponse<List<CollegeSubjectResponse>>> getActive() {
        log.info("API call: GET /college-subject/active");
        List<CollegeSubjectResponse> response = service.getAllActive();
        return ResponseEntity.ok(StandardResponse.success(response, "Active college subjects fetched successfully"));
    }

    /**
     * Get College Subjects by Academic Program
     */
    @GetMapping("/by-program")
    public ResponseEntity<StandardResponse<List<CollegeSubjectResponse>>> getByProgram(@RequestParam String program) {
        log.info("API call: GET /college-subject/by-program - program: {}", program);
        List<CollegeSubjectResponse> response = service.getByProgram(program);
        return ResponseEntity.ok(StandardResponse.success(response, "College subjects for program fetched successfully"));
    }

    /**
     * Get College Subjects by Department ID or Name
     */
    @GetMapping("/by-department")
    public ResponseEntity<StandardResponse<List<CollegeSubjectResponse>>> getByDepartment(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String department
    ) {
        log.info("API call: GET /college-subject/by-department - deptId: {}, deptName: {}", departmentId, department);
        if (departmentId == null && department != null && !department.isBlank()) {
            Department dept = departmentRepository.findByNameIgnoreCase(department.trim()).orElse(null);
            if (dept != null) {
                departmentId = dept.getId();
            }
        }
        if (departmentId == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("departmentId or department name is required", "MISSING_PARAM", null, null));
        }
        List<CollegeSubjectResponse> response = service.getByDepartment(departmentId);
        return ResponseEntity.ok(StandardResponse.success(response, "College subjects for department fetched successfully"));
    }

    /**
     * Filtered & paginated query across all college subjects
     */
    @GetMapping("/getAllSubjects")
    public ResponseEntity<StandardResponse<Page<CollegeSubjectResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer degreeId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer credits
    ) {
        log.info("API call: GET /college-subject/getAllSubjects - page: {}, size: {}, search: {}, deptId: {}, degId: {}, program: {}, sem: {}, type: {}, status: {}",
                page, size, search, departmentId, degreeId, program, semester, type, status);
        Page<CollegeSubjectResponse> response = service.getAll(
                page, size, search, departmentId, degreeId, department, program, semester, academicYear, type, status, credits
        );
        return ResponseEntity.ok(StandardResponse.success(response, "College subjects fetched successfully"));
    }
}
