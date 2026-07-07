package com.academic.service;

import com.academic.dto.mobile.*;
import com.academic.response.StandardResponse;
import java.time.LocalDate;

public interface TeacherMobileService {
    StandardResponse<?> getDashboardData(String employeeId);

    StandardResponse<?> getAttendanceList(Long staffId, LocalDate date, Long classId, Long sectionId);

    StandardResponse<?> submitAttendance(AttendanceSubmissionRequest request);

    /**
     * Returns a per-day attendance breakdown for every day in the given month,
     * plus a whole-month summary.
     *
     * @param classId   the class common-master ID
     * @param sectionId the section common-master ID
     * @param month     1-based month number (1 = Jan … 12 = Dec)
     * @param year      calendar year (e.g. 2026)
     */
    StandardResponse<?> getMonthlyAttendanceCalendar(Long classId, Long sectionId, int month, int year);

    StandardResponse<?> getExamSchedule(Long staffId);

    StandardResponse<?> getStudentListForMarks( Integer examTypeId, Long subjectId);

    StandardResponse<?> saveMarks(EnterMarksRequest request);

    StandardResponse<?> applyLeave(Long staffId, LeaveSubmissionRequest request);

    StandardResponse<?> getLeaveHistory(Long staffId);

    /**
     * Generates a monthly student-attendance Excel (.xlsx) report in the
     * school-register format:
     *   Rows  – one per student
     *   Cols  – Student Name | % | P | L | A | H | F | day-1 … day-N
     */
    byte[] generateAttendanceExcel(Long classId, Long sectionId, int month, int year);

    /**
     * Returns a monthly grid (JSON) representation of student attendance
     * with exactly the same shape and statistics as the Excel download format.
     */
    StandardResponse<?> getMonthlyAttendanceGrid(Long classId, Long sectionId, int month, int year);
}

