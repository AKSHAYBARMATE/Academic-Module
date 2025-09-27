package com.academic.service;

import com.academic.dto.ClassSectionDTO;
import com.academic.request.ClassSectionRequest;
import com.academic.response.ClassSectionResponse;
import org.springframework.data.domain.Page;

public interface ClassSectionService {
    ClassSectionResponse create(ClassSectionRequest request);
    ClassSectionResponse update(Long id, ClassSectionRequest request);
    void delete(Long id);
    ClassSectionResponse getById(Long id);
    Page<ClassSectionResponse> getAll(int page, int size);
}
