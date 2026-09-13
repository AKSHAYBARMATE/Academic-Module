package com.academic.service;

import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubjectService {

    SubjectResponse create(SubjectRequest request);

    SubjectResponse update(Integer id, SubjectRequest request);

    void delete(Integer id);

    SubjectResponse getById(Integer id);

    List<SubjectResponse> getAllActive();

    List<SubjectResponse> getByDepartment(String department);

    Page<SubjectResponse> getAll(
            int page,
            int size,
            String search,
            String department,
            String type,
            String status,
            Integer credits
    );

    // Overloads for legacy callers passing Long
    default SubjectResponse update(Long id, SubjectRequest request) {
        return update(id != null ? id.intValue() : null, request);
    }

    default void delete(Long id) {
        delete(id != null ? id.intValue() : null);
    }

    default SubjectResponse getById(Long id) {
        return getById(id != null ? id.intValue() : null);
    }

    default Page<SubjectResponse> getAll(int page, int size, String search, String type, String status, Integer credits) {
        return getAll(page, size, search, null, type, status, credits);
    }
}
