package com.academic.service;

import com.academic.response.StandardResponse;
import java.time.LocalDate;

public interface StudentMobileService {
    StandardResponse<?> getDashboardData(Long studentId);

    StandardResponse<?> getAttendanceDetails(Long studentId, LocalDate startDate, LocalDate endDate);

    StandardResponse<?> getTimetable(Long studentId, Integer dayOfWeek);

    StandardResponse<?> getExamResults(Long studentId);

    StandardResponse<?> getFeesDetails(Long studentId);
}
