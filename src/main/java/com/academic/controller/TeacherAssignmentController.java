package com.academic.controller;

import com.academic.request.TeacherAssignmentRequest;
import com.academic.response.StandardResponse;
import com.academic.response.TeacherAssignmentResponse;
import com.academic.service.TeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms/api/v1/academic-module")
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentController {

    private final TeacherAssignmentService service;

    @PostMapping("/assignTeacherAllocation")
    public ResponseEntity<StandardResponse<TeacherAssignmentResponse>> create(@RequestBody TeacherAssignmentRequest request) {
        TeacherAssignmentResponse response = service.create(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Assignment created successfully"));
    }

    @PutMapping("/updateTeacherAllocation/{id}")
    public ResponseEntity<StandardResponse<TeacherAssignmentResponse>> update(@PathVariable Long id, @RequestBody TeacherAssignmentRequest request) {
        TeacherAssignmentResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Assignment updated successfully"));
    }

    @DeleteMapping("/deleteTeacherAllocation/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Assignment deleted successfully"));
    }

    @GetMapping("/getTeacherAllocation/{id}")
    public ResponseEntity<StandardResponse<TeacherAssignmentResponse>> getById(@PathVariable Long id) {
        TeacherAssignmentResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Assignment fetched successfully"));
    }

    @GetMapping("/getAllTeacherAllocations")
    public ResponseEntity<StandardResponse<Page<TeacherAssignmentResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String year) {

        Page<TeacherAssignmentResponse> response = service.getAll(page, size, search, status, year);

        return ResponseEntity.ok(
                StandardResponse.success(response, "Assignments fetched successfully")
        );
    }


}
