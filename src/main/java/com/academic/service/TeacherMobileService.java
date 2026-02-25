package com.academic.service;

import com.academic.dto.mobile.*;
import com.academic.response.StandardResponse;
import java.time.LocalDate;

public interface TeacherMobileService {
    StandardResponse<?> getDashboardData(String employeeId);

    StandardResponse<?> getAttendanceList(Long classId, Long sectionId, LocalDate date);

    StandardResponse<?> submitAttendance(AttendanceSubmissionRequest request);
}
