package com.academic.controller;

import com.academic.config.UserContext;
import com.academic.dto.mobile.AttendanceSubmissionRequest;
import com.academic.dto.mobile.EnterMarksRequest;
import com.academic.dto.mobile.LeaveSubmissionRequest;
import com.academic.response.StandardResponse;
import com.academic.service.TeacherMobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/academic-module/teacher-mobile")
@RequiredArgsConstructor
public class TeacherMobileController {

    private final TeacherMobileService teacherMobileService;

    @GetMapping("/getteacherDashboard")
    public ResponseEntity<StandardResponse<?>> getDashboard() {
        Long staffId = UserContext.getStaffId();
        if (staffId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("Staff ID not found for this user", "ID_NOT_FOUND", null));
        }
        return ResponseEntity.ok(teacherMobileService.getDashboardData(String.valueOf(staffId)));
    }

    @GetMapping("/getAttendanceList")
    public ResponseEntity<StandardResponse<?>> getAttendanceList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long staffId = UserContext.getStaffId();
        return ResponseEntity.ok(teacherMobileService.getAttendanceList(staffId, date));
    }

    @PostMapping("/submit-attendance")
    public ResponseEntity<StandardResponse<?>> submitAttendance(@RequestBody AttendanceSubmissionRequest request) {
        return ResponseEntity.ok(teacherMobileService.submitAttendance(request));
    }

    @GetMapping("/getExamSchedule")
    public ResponseEntity<StandardResponse<?>> getExamSchedule() {
        Long staffId = UserContext.getStaffId();
        return ResponseEntity.ok(teacherMobileService.getExamSchedule(staffId));
    }

    @GetMapping("/getStudentListForMarks")
    public ResponseEntity<StandardResponse<?>> getStudentListForMarks(
            @RequestParam Integer examTypeId,
            @RequestParam Long subjectId) {
        return ResponseEntity.ok(teacherMobileService.getStudentListForMarks( examTypeId, subjectId));
    }

    @PostMapping("/saveMarks")
    public ResponseEntity<StandardResponse<?>> saveMarks(@RequestBody EnterMarksRequest request) {
        return ResponseEntity.ok(teacherMobileService.saveMarks(request));
    }

    @PostMapping("/applyLeave")
    public ResponseEntity<StandardResponse<?>> applyLeave(@RequestBody LeaveSubmissionRequest request) {
        Long staffId = UserContext.getStaffId();
        return ResponseEntity.ok(teacherMobileService.applyLeave(staffId, request));
    }

    @GetMapping("/getLeaveHistory")
    public ResponseEntity<StandardResponse<?>> getLeaveHistory() {
        Long staffId = UserContext.getStaffId();
        return ResponseEntity.ok(teacherMobileService.getLeaveHistory(staffId));
    }
}
