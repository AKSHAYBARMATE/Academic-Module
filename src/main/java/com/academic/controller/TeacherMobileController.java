package com.academic.controller;

import com.academic.config.UserContext;
import com.academic.dto.mobile.AttendanceSubmissionRequest;
import com.academic.dto.mobile.EnterMarksRequest;
import com.academic.dto.mobile.LeaveSubmissionRequest;
import com.academic.response.StandardResponse;
import com.academic.service.TeacherMobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId) {
        // If classId and sectionId are both provided, staffId is not required
        Long staffId = (classId != null && sectionId != null) ? null : UserContext.getStaffId();
        return ResponseEntity.ok(teacherMobileService.getAttendanceList(staffId, date, classId, sectionId));
    }

    @PostMapping("/submit-attendance")
    public ResponseEntity<StandardResponse<?>> submitAttendance(@RequestBody AttendanceSubmissionRequest request) {
        return ResponseEntity.ok(teacherMobileService.submitAttendance(request));
    }

    /**
     * GET /api/v1/academic-module/teacher-mobile/getMonthlyAttendanceCalendar
     *
     * Query params:
     *   classId   – common-master ID of the class  (required)
     *   sectionId – common-master ID of the section (required)
     *   month     – 1..12                           (required)
     *   year      – e.g. 2026                       (required)
     *
     * Returns per-day present/absent counts + student lists + monthly % summary.
     */
    @GetMapping("/getMonthlyAttendanceCalendar")
    public ResponseEntity<StandardResponse<?>> getMonthlyAttendanceCalendar(
            @RequestParam Long classId,
            @RequestParam Long sectionId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                teacherMobileService.getMonthlyAttendanceCalendar(classId, sectionId, month, year));
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

    /**
     * GET /api/v1/academic-module/teacher-mobile/downloadAttendanceExcel
     *
     * Query params:
     *   classId   – common-master ID of the class   (required)
     *   sectionId – common-master ID of the section  (required)
     *   month     – 1..12                            (required)
     *   year      – e.g. 2026                        (required)
     *
     * Downloads a .xlsx Student Attendance Report with one row per student and
     * one column per day of the month (P / A / H / -) plus summary columns.
     */
    @GetMapping("/downloadAttendanceExcel")
    public ResponseEntity<byte[]> downloadAttendanceExcel(
            @RequestParam Long classId,
            @RequestParam Long sectionId,
            @RequestParam int month,
            @RequestParam int year) {

        byte[] excel = teacherMobileService.generateAttendanceExcel(classId, sectionId, month, year);

        String fileName = "attendance_" + year + "_" + String.format("%02d", month) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}