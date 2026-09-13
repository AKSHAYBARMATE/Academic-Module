package com.academic.service;

import com.academic.request.CollegeSubjectRequest;
import com.academic.response.CollegeSubjectResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CollegeSubjectService {

    CollegeSubjectResponse create(CollegeSubjectRequest request);

    CollegeSubjectResponse update(Long id, CollegeSubjectRequest request);

    void delete(Long id);

    CollegeSubjectResponse getById(Long id);

    List<CollegeSubjectResponse> getAllActive();

    List<CollegeSubjectResponse> getByProgram(String program);

    List<CollegeSubjectResponse> getByDepartment(Integer departmentId);

    Page<CollegeSubjectResponse> getAll(
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
}
