package com.academic.service;


import com.academic.entity.TimeTable;
import com.academic.request.TimeTableRequest;
import com.academic.response.StandardResponse;
import com.academic.response.TimeTableResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import java.util.List;

public interface TimeTableService {

    TimeTableResponse create(TimeTableRequest request);

    TimeTableResponse get(Long id);

    TimeTableResponse update(Long id, TimeTableRequest request);

    void delete(Long id);

    @Transactional(readOnly = true)
    StandardResponse<List<TimeTableResponse>> listAll(
            Integer page,
            Integer size,
            Long classId,
            Long section,
            String search);
}
