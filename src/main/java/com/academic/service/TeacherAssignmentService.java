package com.academic.service;

import com.academic.request.TeacherAssignmentRequest;
import com.academic.response.TeacherAssignmentResponse;
import org.springframework.data.domain.Page;

public interface TeacherAssignmentService {
    TeacherAssignmentResponse create(TeacherAssignmentRequest request);
    TeacherAssignmentResponse update(Long id, TeacherAssignmentRequest request);
    void delete(Long id);
    TeacherAssignmentResponse getById(Long id);

    Page<TeacherAssignmentResponse> getAll(int page, int size, String search, String status, String year);
}
