package com.academic.service;

import com.academic.request.DegreeRequest;
import com.academic.response.DegreeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DegreeService {

    DegreeResponse create(DegreeRequest request);

    DegreeResponse update(Integer id, DegreeRequest request);

    void delete(Integer id);

    DegreeResponse getById(Integer id);

    List<DegreeResponse> getAllActive();

    Page<DegreeResponse> getAll(int page, int size, String search, String degreeType, String status);
}
