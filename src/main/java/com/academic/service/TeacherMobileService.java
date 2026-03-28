package com.academic.service;

import com.academic.dto.mobile.*;
import com.academic.response.StandardResponse;
import java.time.LocalDate;

public interface TeacherMobileService {
    StandardResponse<?> getDashboardData(String employeeId);

    StandardResponse<?> getAttendanceList(Long staffId, LocalDate date);

    StandardResponse<?> submitAttendance(AttendanceSubmissionRequest request);

    StandardResponse<?> getExamSchedule(Long staffId);

    StandardResponse<?> getStudentListForMarks( Integer examTypeId, Long subjectId);

    StandardResponse<?> saveMarks(EnterMarksRequest request);

    StandardResponse<?> applyLeave(Long staffId, LeaveSubmissionRequest request);

    StandardResponse<?> getLeaveHistory(Long staffId);
}
