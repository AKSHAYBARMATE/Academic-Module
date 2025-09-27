package com.academic.controller;

import com.academic.dto.ClassSectionDTO;
import com.academic.request.ClassSectionRequest;
import com.academic.response.ClassSectionResponse;
import com.academic.response.StandardResponse;
import com.academic.service.ClassSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module")
@RequiredArgsConstructor
@Slf4j
public class ClassSectionController {

    private final ClassSectionService service;

    @PostMapping("/createClassSection")
    public ResponseEntity<StandardResponse<ClassSectionResponse>> create(@RequestBody ClassSectionRequest request) {
        log.info("API - Create ClassSection: {}", request);
        ClassSectionResponse created = service.create(request);
        return ResponseEntity.ok(StandardResponse.success(created, "Class section created successfully"));
    }

    @PutMapping("/updateClassSection/{id}")
    public ResponseEntity<StandardResponse<ClassSectionResponse>> update(@PathVariable Long id,
                                                                         @RequestBody ClassSectionRequest request) {
        log.info("API - Update ClassSection with id: {}", id);
        ClassSectionResponse updated = service.update(id, request);
        return ResponseEntity.ok(StandardResponse.success(updated, "Class section updated successfully"));
    }

    @DeleteMapping("/deleteClass/{id}")
    public ResponseEntity<StandardResponse<Void>> delete(@PathVariable Long id) {
        log.info("API - Delete ClassSection with id: {}", id);
        service.delete(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Class section deleted successfully"));
    }

    @GetMapping("/getClassConfigDetailById/{id}")
    public ResponseEntity<StandardResponse<ClassSectionResponse>> getById(@PathVariable Long id) {
        log.info("API - Get ClassSection by id: {}", id);
        ClassSectionResponse response = service.getById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Class section fetched successfully"));
    }

    @GetMapping("/getAllClassSections")
    public ResponseEntity<StandardResponse<Page<ClassSectionResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size ){
        log.info("API - Get all ClassSections. Page: {}, Size: {}, Active: {}", page, size);
        Page<ClassSectionResponse> result = service.getAll(page, size);
        return ResponseEntity.ok(StandardResponse.success(result, "Class sections fetched successfully"));
    }
}

