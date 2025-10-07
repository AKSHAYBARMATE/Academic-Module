package com.academic.service;


import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;
import org.springframework.data.domain.Page;

public interface SubjectService {

    SubjectResponse create(SubjectRequest request);

    SubjectResponse update(Long id, SubjectRequest request);

    void delete(Long id);

    SubjectResponse getById(Long id);

    Page<SubjectResponse> getAll(int page, int size, String search, String type, String status, Integer credits);
}
