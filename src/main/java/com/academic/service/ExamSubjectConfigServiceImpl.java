package com.academic.service;

import com.academic.entity.CommonMaster;
import com.academic.entity.ExamSubjectConfig;
import com.academic.entity.Session;
import com.academic.entity.Subject;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.ExamSubjectConfigRepository;
import com.academic.repository.SessionRepository;
import com.academic.repository.SubjectRepository;
import com.academic.request.*;
import com.academic.response.ExamSubjectConfigResponse;
import com.academic.response.StandardResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSubjectConfigServiceImpl implements ExamSubjectConfigService{

    private final ExamSubjectConfigRepository repository;
    private final SessionRepository sessionRepository;
    private final CommonMasterRepository examTypeRepository;
    private final SubjectRepository subjectRepository;

    /* ================= BULK CREATE ================= */

    public StandardResponse<List<ExamSubjectConfigResponse>> createBulk(
            ExamSubjectConfigBulkRequest request
    ) {

        Session session = sessionRepository.findById(request.getSessionId().longValue())
                .orElseThrow(() -> new RuntimeException("Invalid Session"));

        CommonMaster examType = examTypeRepository.findById(request.getExamTypeId())
                .orElseThrow(() -> new RuntimeException("Invalid Exam Type"));

        List<ExamSubjectConfigResponse> responses = new ArrayList<>();

        for (SubjectMarksRequest s : request.getSubjects()) {

            Subject subject = subjectRepository.findById(s.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Invalid Subject"));

            repository.findBySession_IdAndExamType_IdAndSubject_Id(
                    session.getId(),
                    examType.getId(),
                    subject.getId()
            ).ifPresent(e -> {
                throw new RuntimeException(
                        "Configuration already exists for subject: "
                                + subject.getSubjectName()
                );
            });

            int total =
                    s.getTheoryMarks()
                            + s.getPracticalMarks()
                            + s.getInternalMarks();

            ExamSubjectConfig config = ExamSubjectConfig.builder()
                    .session(session)
                    .examType(examType)
                    .subject(subject)
                    .theoryMarks(s.getTheoryMarks())
                    .practicalMarks(s.getPracticalMarks())
                    .internalMarks(s.getInternalMarks())
                    .totalMarks(total)
                    .build();

            repository.save(config);
            responses.add(map(config));
        }

        return StandardResponse.success(
                responses,
                "Exam subject configurations saved successfully"
        );
    }

    /* ================= READ ================= */

    public StandardResponse<List<ExamSubjectConfigResponse>> getAll(
            Integer sessionId,
            Integer examTypeId
    ) {
        return StandardResponse.success(
                repository.findBySession_IdAndExamType_IdAndIsDeleteFalse(
                        sessionId, examTypeId
                ).stream().map(this::map).toList(),
                "Configurations fetched"
        );
    }

    /* ================= UPDATE ================= */

    @Transactional
    @Override
    public StandardResponse<?> updateBulk(
            ExamSubjectMarksBulkUpdateRequest request
    ) {

        List<ExamSubjectConfigResponse> responses = new ArrayList<>();

        for (SubjectMarksUpdateRequest s : request.getSubjects()) {
            ExamSubjectConfig config;
            if(s.getId()!=null){
                config =  repository.findById(s.getId())
                        .orElseThrow(() ->
                                new RuntimeException("Config not found for id: " + s.getId())
                        );
            } else {
                config = new ExamSubjectConfig();
                config.setSubject(subjectRepository.findById(s.getSubjectId()).get());
                config.setSession(sessionRepository.findById(s.getSessionId()).get());
                config.setExamType(examTypeRepository.findById(s.getExamTypeId()).get());
            }

            config.setTheoryMarks(s.getTheoryMarks());
            config.setPracticalMarks(s.getPracticalMarks());
            config.setInternalMarks(s.getInternalMarks());

            config.setTotalMarks(
                    s.getTheoryMarks()
                            + s.getPracticalMarks()
                            + s.getInternalMarks()
            );

            repository.save(config);
            responses.add(map(config));
        }

        return StandardResponse.success(
                responses,
                "Exam subject marks updated successfully"
        );
    }


    /* ================= DELETE ================= */

    public StandardResponse<Void> delete(Long id) {
        ExamSubjectConfig config = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found"));

        config.setIsDelete(true);
        repository.save(config);

        return StandardResponse.success("Configuration deleted");
    }

    /* ================= MAPPER ================= */

    private ExamSubjectConfigResponse map(ExamSubjectConfig c) {
        return ExamSubjectConfigResponse.builder()
                .id(c.getId())
                .session(c.getSession().getSession())
                .examType(c.getExamType().getData())
                .examTypeId(c.getExamType().getId())
                .subjectId(c.getSubject().getId())
                .subjectCode(c.getSubject().getSubjectCode())
                .subjectName(c.getSubject().getSubjectName())
                .theoryMarks(c.getTheoryMarks())
                .practicalMarks(c.getPracticalMarks())
                .internalMarks(c.getInternalMarks())
                .totalMarks(c.getTotalMarks())
                .build();
    }
}
