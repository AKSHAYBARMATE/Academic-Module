package com.academic.controller;

import com.academic.request.ProgramRequest;
import com.academic.response.ProgramResponse;
import com.academic.response.StandardResponse;
import com.academic.service.ProgramService;
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
public class ProgramController {

    private final ProgramService service;

    /**
     * Create a new Academic Program / Course
     */
    @PostMapping("/createProgram")
    public ResponseEntity<StandardResponse<ProgramResponse>> create(@RequestBody ProgramRequest request) {
        log.info("API call: POST /createProgram - code: {}, name: {}",
                request != null ? request.getCode() : null,
                request != null ? request.getName() : null);
        ProgramResponse response = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(response, "Program created successfully"));
    }

    /**
     * Update an existing Academic Program / Course by ID
     */
    @PutMapping("/updateProgram/{id}")
    public ResponseEntity<StandardResponse<ProgramResponse>> update(
            @PathVariable Integer id,
            @RequestBody ProgramRequest request
    ) {
        log.info("API call: PUT /updateProgram/{} - payload: {}", id, request);
        ProgramResponse response = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Program updated successfully"));
    }

    /**
     * Soft delete an Academic Program / Course by ID
     */
    @DeleteMapping("/deleteProgram/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Integer id) {
        log.warn("API call: DELETE /deleteProgram/{}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success("Program deleted successfully"));
    }

    /**
     * Fetch Program details by ID
     */
    @GetMapping("/getProgramById/{id}")
    public ResponseEntity<StandardResponse<ProgramResponse>> getById(@PathVariable Integer id) {
        log.info("API call: GET /getProgramById/{}", id);
        ProgramResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Program fetched successfully"));
    }

    /**
     * Fetch all active programs for dropdown selectors
     */
    @GetMapping("/getActivePrograms")
    public ResponseEntity<StandardResponse<List<ProgramResponse>>> getActivePrograms() {
        log.info("API call: GET /getActivePrograms");
        List<ProgramResponse> response = service.getAllActive();
        return ResponseEntity.ok(StandardResponse.success(response, "Active programs fetched successfully"));
    }

    /**
     * Fetch all programs affiliated under a specific Degree Code (e.g. B.Tech)
     */
    @GetMapping("/getProgramsByDegree/{degreeCode}")
    public ResponseEntity<StandardResponse<List<ProgramResponse>>> getProgramsByDegree(
            @PathVariable String degreeCode
    ) {
        log.info("API call: GET /getProgramsByDegree/{}", degreeCode);
        List<ProgramResponse> response = service.getByDegreeCode(degreeCode);
        return ResponseEntity.ok(StandardResponse.success(response, "Programs for degree fetched successfully"));
    }

    /**
     * Fetch paginated and filtered list of Programs / Courses
     */
    @GetMapping("/getAllPrograms")
    public ResponseEntity<StandardResponse<Page<ProgramResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String degreeCode,
            @RequestParam(required = false) String degreeType,
            @RequestParam(required = false) String status
    ) {
        log.info("API call: GET /getAllPrograms - page: {}, size: {}, search: {}, degreeCode: {}, degreeType: {}, status: {}",
                page, size, search, degreeCode, degreeType, status);
        Page<ProgramResponse> response = service.getAll(page, size, search, degreeCode, degreeType, status);
        return ResponseEntity.ok(StandardResponse.success(response, "Programs fetched successfully"));
    }
}
