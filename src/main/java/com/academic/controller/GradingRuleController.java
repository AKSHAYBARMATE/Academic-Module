package com.academic.controller;

import com.academic.request.GradingRuleRequest;
import com.academic.response.StandardResponse;
import com.academic.service.GradingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module/grading-rules")
@RequiredArgsConstructor
public class GradingRuleController {

    private final GradingRuleService service;

    @PostMapping("/saveGrading")
    public StandardResponse<?> create(@RequestBody GradingRuleRequest request) {
        return service.create(request);
    }

    @GetMapping("/getAllGrading")
    public StandardResponse<?> getAll() {
        return service.getAll();
    }

    @PutMapping("updateGrading/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @RequestBody GradingRuleRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("deleteGrading/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
