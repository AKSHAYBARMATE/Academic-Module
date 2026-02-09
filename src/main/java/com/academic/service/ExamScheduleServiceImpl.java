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
@RequiredArgsConstructor
@Transactional
public class ExamScheduleServiceImpl implements ExamScheduleService{

    private final ExamScheduleRepository repository;
    private final SessionRepository sessionRepository;
    private final CommonMasterRepository examTypeRepository;

    /* CREATE */
    public StandardResponse<?> create(ExamScheduleRequest request) {

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Invalid session"));

        CommonMaster examType = examTypeRepository.findById(request.getExamTypeId())
                .orElseThrow(() -> new RuntimeException("Invalid exam type"));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        ExamSchedule schedule = ExamSchedule.builder()
                .examTitle(request.getExamTitle())
                .session(session)
                .examType(examType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .build();

        repository.save(schedule);

        return StandardResponse.success(map(schedule), "Exam schedule created");
    }

    /* READ */
    public StandardResponse<?> getAll(Integer sessionId) {
        return StandardResponse.success(
                repository.findBySession_IdAndIsActiveTrue(sessionId)
                        .stream().map(this::map).toList(),
                "Exam schedules fetched"
        );
    }

    /* UPDATE */
    public StandardResponse<?> update(Long id, ExamScheduleRequest request) {

        ExamSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setExamTitle(request.getExamTitle());
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setStatus(request.getStatus());

        repository.save(schedule);

        return StandardResponse.success(map(schedule), "Exam schedule updated");
    }

    /* DELETE (Soft) */
    public StandardResponse<?> delete(Long id) {

        ExamSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setIsActive(false);
        repository.save(schedule);

        return StandardResponse.success("Exam schedule deleted");
    }

    private ExamScheduleResponse map(ExamSchedule s) {
        return ExamScheduleResponse.builder()
                .id(s.getId())
                .examTitle(s.getExamTitle())
                .session(s.getSession().getSession())
                .examType(s.getExamType().getData())
                .examTypeId(s.getExamType().getId())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .build();
    }
}
