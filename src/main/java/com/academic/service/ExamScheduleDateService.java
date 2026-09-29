package com.academic.service;

import com.academic.request.ExamScheduleDateRequest;
import com.academic.response.StandardResponse;

import java.time.LocalDate;
import java.util.List;

public interface ExamScheduleDateService {

    StandardResponse<?> save(ExamScheduleDateRequest request);

    StandardResponse<?> bulkSave(List<ExamScheduleDateRequest> requests);

    StandardResponse<?> getById(Long id);

    StandardResponse<?> getAll(
            Long examScheduleId,
            Integer sessionId,
            Integer classId,
            Integer sectionId,
            LocalDate examDate,
            LocalDate startDate,
            LocalDate endDate
    );

    StandardResponse<?> delete(Long id);

    byte[] generateExamTimeTablePdf(
            Long examScheduleId,
            Integer sessionId,
            Integer classId,
            Integer sectionId,
            LocalDate examDate,
            LocalDate startDate,
            LocalDate endDate
    );
}
