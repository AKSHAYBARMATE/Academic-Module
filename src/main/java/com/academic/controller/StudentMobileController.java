package com.academic.controller;

import com.academic.config.UserContext;
import com.academic.response.StandardResponse;
import com.academic.service.StudentMobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/academic-module/student-mobile")
@RequiredArgsConstructor
public class StudentMobileController {

    private final StudentMobileService studentMobileService;

    @GetMapping("/dashboard")
    public ResponseEntity<StandardResponse<?>> getDashboard() {
        Long studentId = UserContext.getStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Student ID not found for this user", "ID_NOT_FOUND", null));
        }
        return ResponseEntity.ok(studentMobileService.getDashboardData(studentId));
    }

    @GetMapping("/attendance")
    public ResponseEntity<StandardResponse<?>> getAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long studentId = UserContext.getStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Student ID not found for this user", "ID_NOT_FOUND", null));
        }
        return ResponseEntity.ok(studentMobileService.getAttendanceDetails(studentId, startDate, endDate));
    }

    @GetMapping("/timetable")
    public ResponseEntity<StandardResponse<?>> getTimetable(
            @RequestParam Integer dayOfWeek) {
        Long studentId = UserContext.getStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Student ID not found for this user", "ID_NOT_FOUND", null));
        }
        return ResponseEntity.ok(studentMobileService.getTimetable(studentId, dayOfWeek));
    }

    @GetMapping("/exam-results")
    public ResponseEntity<StandardResponse<?>> getExamResults() {
        Long studentId = UserContext.getStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Student ID not found for this user", "ID_NOT_FOUND", null));
        }
        return ResponseEntity.ok(studentMobileService.getExamResults(studentId));
    }

    @GetMapping("/fees")
    public ResponseEntity<StandardResponse<?>> getFees() {
        Long studentId = UserContext.getStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Student ID not found for this user", "ID_NOT_FOUND", null));
        }
        return ResponseEntity.ok(studentMobileService.getFeesDetails(studentId));
    }
}
