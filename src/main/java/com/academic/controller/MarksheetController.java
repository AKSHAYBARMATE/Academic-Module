package com.academic.controller;


import com.academic.dto.MarksheetRequest;
import com.academic.response.StandardResponse;
import com.academic.service.MarksheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module/marksheets")
@RequiredArgsConstructor
public class MarksheetController {

    private final MarksheetService service;

    @PostMapping("/createMarksheet")
    public StandardResponse<?> create(@RequestBody MarksheetRequest request) {
        return service.create(request);
    }

    @PutMapping("/updateMarksheet/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @RequestBody MarksheetRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/getAllMarksheet")
    public StandardResponse<?> getAll(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer examTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getAll(classId, examTypeId, page, size);
    }

    @GetMapping("getMarksheetById/{id}")
    public StandardResponse<?> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("deleteMarksheet/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
