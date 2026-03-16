package com.academic.service;

import com.academic.request.ExamSubjectConfigBulkRequest;
import com.academic.response.ExamSubjectConfigResponse;
import com.academic.request.ExamSubjectMarksBulkUpdateRequest;

import com.academic.response.StandardResponse;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExamSubjectConfigService {

        /* ================= BULK CREATE ================= */

        public StandardResponse createBulk(
                        ExamSubjectConfigBulkRequest request);

        /* ================= READ ================= */

        public StandardResponse<List<ExamSubjectConfigResponse>> getAll(
                        Integer sessionId,
                        Integer examTypeId,
                        Integer classId);

        /* ================= UPDATE ================= */

        @Transactional
        StandardResponse<?> updateBulk(
                        ExamSubjectMarksBulkUpdateRequest request);

        /* ================= DELETE ================= */

        public StandardResponse<Void> delete(Long id);

    StandardResponse<?> getAllComponents();

    StandardResponse<?> getActivityMasters();
}
