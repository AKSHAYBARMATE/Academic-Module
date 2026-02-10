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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamSubjectConfigServiceImpl implements ExamSubjectConfigService {

    private final ExamSubjectConfigRepository repository;
    private final SessionRepository sessionRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final SubjectRepository subjectRepository;

    /* ================= BULK CREATE ================= */
    public StandardResponse createBulk(
            ExamSubjectConfigBulkRequest request) {

        /* ================= Validate Session ================= */
        Optional<Session> sessionOpt =
                sessionRepository.findById(request.getSessionId().longValue());

        if (sessionOpt.isEmpty()) {
            return StandardResponse.error(
                    "Invalid Session",
                    "INVALID_SESSION",
                    "sessionId",
                    "No session found with id " + request.getSessionId()
            );
        }

        /* ================= Validate Exam Type ================= */
        Optional<CommonMaster> examTypeOpt =
                commonMasterRepository.findById(request.getExamTypeId());

        if (examTypeOpt.isEmpty()) {
            return StandardResponse.error(
                    "Invalid Exam Type",
                    "INVALID_EXAM_TYPE",
                    "examTypeId",
                    "No exam type found with id " + request.getExamTypeId()
            );
        }

        /* ================= Validate Class ================= */
        Optional<CommonMaster> classOpt =
                commonMasterRepository.findById(request.getClassId());

        if (classOpt.isEmpty()) {
            return StandardResponse.error(
                    "Invalid Class",
                    "INVALID_CLASS",
                    "classId",
                    "No class found with id " + request.getClassId()
            );
        }

        Session session = sessionOpt.get();
        CommonMaster examType = examTypeOpt.get();
        CommonMaster classMaster = classOpt.get();

        List<ExamSubjectConfigResponse> responses = new ArrayList<>();

        /* ================= Loop Subjects ================= */
        for (SubjectMarksRequest s : request.getSubjects()) {

            Optional<Subject> subjectOpt =
                    subjectRepository.findById(s.getSubjectId());

            if (subjectOpt.isEmpty()) {
                return StandardResponse.error(
                        "Invalid Subject",
                        "INVALID_SUBJECT",
                        "subjectId",
                        "No subject found with id " + s.getSubjectId()
                );
            }

            Subject subject = subjectOpt.get();

            boolean exists =
                    repository.findBySession_IdAndExamType_IdAndSubject_IdAndClassIdId(
                            session.getId(),
                            examType.getId(),
                            subject.getId(),
                            classMaster.getId()
                    ).isPresent();

            if (exists) {
                return StandardResponse.error(
                        "Configuration already exists",
                        "DUPLICATE_CONFIG",
                        "subjectId",
                        "Already configured for subject: " + subject.getSubjectName()
                );
            }

            int total = s.getTheoryMarks()
                    + s.getPracticalMarks()
                    + s.getInternalMarks();

            ExamSubjectConfig config = ExamSubjectConfig.builder()
                    .session(session)
                    .examType(examType)
                    .classId(classMaster)
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
            Integer examTypeId) {
        return StandardResponse.success(
                repository.findBySession_IdAndExamType_IdAndIsDeleteFalse(
                        sessionId, examTypeId).stream().map(this::map).toList(),
                "Configurations fetched");
    }

    /* ================= UPDATE ================= */

    @Transactional
    @Override
    public StandardResponse<?> updateBulk(
            ExamSubjectMarksBulkUpdateRequest request) {

        List<ExamSubjectConfigResponse> responses = new ArrayList<>();

        for (SubjectMarksUpdateRequest s : request.getSubjects()) {
            ExamSubjectConfig config;
            if (s.getId() != null) {
                config = repository.findById(s.getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Config not found for id: " + s.getId()));
            } else {
                config = new ExamSubjectConfig();
                config.setSubject(subjectRepository.findById(s.getSubjectId()).get());
                config.setSession(sessionRepository.findById(s.getSessionId()).get());
                config.setExamType(commonMasterRepository.findById(s.getExamTypeId()).get());
                config.setClassId(commonMasterRepository.findById(s.getClassId()).get());
            }

            config.setTheoryMarks(s.getTheoryMarks());
            config.setPracticalMarks(s.getPracticalMarks());
            config.setInternalMarks(s.getInternalMarks());

            config.setTotalMarks(
                    s.getTheoryMarks()
                            + s.getPracticalMarks()
                            + s.getInternalMarks());

            repository.save(config);
            responses.add(map(config));
        }

        return StandardResponse.success(
                responses,
                "Exam subject marks updated successfully");
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

        return c == null ? null : ExamSubjectConfigResponse.builder()
                .id(c.getId())

                .session(c.getSession() == null ? null : c.getSession().getSession())

                .examType(c.getExamType() == null ? null : c.getExamType().getData())
                .examTypeId(c.getExamType() == null ? null : c.getExamType().getId())

                .classId(c.getClassId() == null ? null : c.getClassId().getId())
                .className(c.getClassId() == null ? null : c.getClassId().getData())

                .subjectId(c.getSubject() == null ? null : c.getSubject().getId())
                .subjectCode(c.getSubject() == null ? null : c.getSubject().getSubjectCode())
                .subjectName(c.getSubject() == null ? null : c.getSubject().getSubjectName())

                .theoryMarks(c.getTheoryMarks())
                .practicalMarks(c.getPracticalMarks())
                .internalMarks(c.getInternalMarks())
                .totalMarks(c.getTotalMarks())
                .build();
    }

}
