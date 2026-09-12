package com.academic.controller;

import com.academic.request.DegreeRequest;
import com.academic.response.DegreeResponse;
import com.academic.response.StandardResponse;
import com.academic.service.DegreeService;
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
public class DegreeController {

    private final DegreeService service;

    /**
     * Create a new Degree record
     */
    @PostMapping("/createDegree")
    public ResponseEntity<StandardResponse<DegreeResponse>> create(@RequestBody DegreeRequest request) {
        log.info("API call: POST /createDegree - code: {}", request != null ? request.getCode() : null);
        DegreeResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(response, "Degree created successfully"));
    }

    /**
     * Update an existing Degree record by ID
     */
    @PutMapping("/updateDegree/{id}")
    public ResponseEntity<StandardResponse<DegreeResponse>> update(
            @PathVariable Integer id,
            @RequestBody DegreeRequest request
    ) {
        log.info("API call: PUT /updateDegree/{} - payload: {}", id, request);
        DegreeResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Degree updated successfully"));
    }

    /**
     * Soft delete a Degree record by ID
     */
    @DeleteMapping("/deleteDegree/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Integer id) {
        log.warn("API call: DELETE /deleteDegree/{}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Degree deleted successfully"));
    }

    /**
     * Fetch Degree record by ID
     */
    @GetMapping("/getDegreeById/{id}")
    public ResponseEntity<StandardResponse<DegreeResponse>> getById(@PathVariable Integer id) {
        log.info("API call: GET /getDegreeById/{}", id);
        DegreeResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Degree fetched successfully"));
    }

    /**
     * List all active degrees (for dropdowns / quick selectors)
     */
    @GetMapping("/getActiveDegrees")
    public ResponseEntity<StandardResponse<List<DegreeResponse>>> getActiveDegrees() {
        log.info("API call: GET /getActiveDegrees");
        List<DegreeResponse> response = service.getAllActive();
        return ResponseEntity.ok(StandardResponse.success(response, "Active degrees fetched successfully"));
    }

    /**
     * Fetch paginated and filtered degrees list
     */
    @GetMapping("/getAllDegrees")
    public ResponseEntity<StandardResponse<Page<DegreeResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String degreeType,
            @RequestParam(required = false) String status
    ) {
        log.info("API call: GET /getAllDegrees - page: {}, size: {}, search: {}, degreeType: {}, status: {}",
                page, size, search, degreeType, status);
        Page<DegreeResponse> response = service.getAll(page, size, search, degreeType, status);
        return ResponseEntity.ok(StandardResponse.success(response, "Degrees fetched successfully"));
    }
}
