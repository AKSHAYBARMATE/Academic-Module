package com.academic.service;

import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubjectService {

    SubjectResponse create(SubjectRequest request);

    SubjectResponse update(Long id, SubjectRequest request);

    default SubjectResponse update(Integer id, SubjectRequest request) {
        return update(id != null ? id.longValue() : null, request);
    }

    void delete(Long id);

    default void delete(Integer id) {
        delete(id != null ? id.longValue() : null);
    }

    SubjectResponse getById(Long id);

    default SubjectResponse getById(Integer id) {
        return getById(id != null ? id.longValue() : null);
    }

    List<SubjectResponse> getAllActive();

    List<SubjectResponse> getByProgram(String program);

    List<SubjectResponse> getByDepartment(String department);

    List<SubjectResponse> getByDegree(Integer degreeId);

    List<SubjectResponse> getByDepartmentId(Integer departmentId);

    Page<SubjectResponse> getAll(
            int page,
            int size,
            String search,
            Integer departmentId,
            Integer degreeId,
            String department,
            String program,
            String semester,
            String academicYear,
            String type,
            String status,
            Integer credits
    );

    default Page<SubjectResponse> getAll(
            int page,
            int size,
            String search,
            String department,
            String program,
            String semester,
            String academicYear,
            String type,
            String status,
            Integer credits
    ) {
        return getAll(page, size, search, null, null, department, program, semester, academicYear, type, status, credits);
    }

    default Page<SubjectResponse> getAll(int page, int size, String search, String type, String status, Integer credits) {
        return getAll(page, size, search, null, null, null, null, null, null, type, status, credits);
    }
}
