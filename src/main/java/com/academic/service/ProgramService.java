package com.academic.service;

import com.academic.request.ProgramRequest;
import com.academic.response.ProgramResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProgramService {

    ProgramResponse create(ProgramRequest request);

    ProgramResponse update(Integer id, ProgramRequest request);

    void delete(Integer id);

    ProgramResponse getById(Integer id);

    List<ProgramResponse> getAllActive();

    List<ProgramResponse> getByDegreeCode(String degreeCode);

    Page<ProgramResponse> getAll(int page, int size, String search, String degreeCode, String degreeType, String status);
}
