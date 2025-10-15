package com.academic.service;

import com.academic.request.ExamSetupRequest;
import com.academic.response.ExamSetupResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExamSetupService {

    ExamSetupResponse create(ExamSetupRequest request);

    ExamSetupResponse update(Long id, ExamSetupRequest request);

    ExamSetupResponse getById(Long id);

    void delete(Long id); // soft delete

    Page<ExamSetupResponse> getAllFiltered(String examName, Long classId, Long academicId, int page, int size);
}
