package com.academic.controller;

import com.academic.request.ExamScheduleRequest;
import com.academic.response.StandardResponse;
import com.academic.service.ExamScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final ExamScheduleService service;

    @PostMapping("/saveExamSchedule")
    public StandardResponse<?> create(
            @RequestBody ExamScheduleRequest request
    ) {
        return service.create(request);
    }

    @GetMapping("/getAllExamSchedule")
    public StandardResponse<?> getAll(
            @RequestParam Integer sessionId
    ) {
        return service.getAll(sessionId);
    }

    @PutMapping("/updateExamSchedule/{id}")
    public StandardResponse<?> update(
            @PathVariable Long id,
            @RequestBody ExamScheduleRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/deleteExamSchedule/{id}")
    public StandardResponse<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
