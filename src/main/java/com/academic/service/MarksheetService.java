package com.academic.service;

import com.academic.dto.MarksheetRequest;
import com.academic.response.StandardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface MarksheetService {


    /* CREATE */
    @Transactional
    public StandardResponse<?> create(MarksheetRequest request);

    /* UPDATE (SAME AS SAVE) */
    @Transactional
    public StandardResponse<?> update(Long id, MarksheetRequest request);

    /* GET ALL (PAGINATION + FILTERS) */
    public StandardResponse<?> getAll(
            Integer classId,
            Integer examTypeId,
            int page,
            int size
    );

    public StandardResponse<?> getById(Long id);

    public StandardResponse<?> delete(Long id);
}
