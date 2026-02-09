package com.academic.service;

import com.academic.entity.CommonMaster;
import com.academic.entity.ExamSchedule;
import com.academic.entity.Session;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.ExamScheduleRepository;
import com.academic.repository.SessionRepository;
import com.academic.request.ExamScheduleRequest;
import com.academic.response.ExamScheduleResponse;
import com.academic.response.StandardResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
public interface ExamScheduleService {


    /* CREATE */
    public StandardResponse<?> create(ExamScheduleRequest request);

    /* READ */
    public StandardResponse<?> getAll(Integer sessionId);

    /* UPDATE */
    public StandardResponse<?> update(Long id, ExamScheduleRequest request);

    /* DELETE (Soft) */
    public StandardResponse<?> delete(Long id);

}
