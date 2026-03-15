package com.academic.service;

import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.request.*;
import com.academic.response.ComponentConfigResponse;
import com.academic.response.ExamSubjectConfigResponse;
import com.academic.response.StandardResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamSubjectConfigServiceImpl implements ExamSubjectConfigService {

    @Autowired
        private final ExamSubjectConfigRepository repository;
    @Autowired
        private final SessionRepository sessionRepository;
    @Autowired
        private final CommonMasterRepository commonMasterRepository;
    @Autowired
        private final SubjectRepository subjectRepository;
        @Autowired
        private ExamSubjectConfigComponentRepository componentConfigRepository;

    @Autowired
    private ExamComponentMasterRepostory componentMasterRepository;

    /* ================= BULK CREATE ================= */
        @Transactional
        public StandardResponse<?> createBulk(ExamSubjectConfigBulkRequest request) {

            /* ================= Validate Session ================= */

            Session session = sessionRepository
                    .findById(request.getSessionId())
                    .orElse(null);

            if (session == null) {
                return StandardResponse.error(
                        "Invalid Session",
                        "INVALID_SESSION",
                        "sessionId",
                        "No session found with id " + request.getSessionId());
            }

            /* ================= Validate Exam Type ================= */

            CommonMaster examType = commonMasterRepository
                    .findById(request.getExamTypeId())
                    .orElse(null);

            if (examType == null) {
                return StandardResponse.error(
                        "Invalid Exam Type",
                        "INVALID_EXAM_TYPE",
                        "examTypeId",
                        "No exam type found with id " + request.getExamTypeId());
            }

            /* ================= Validate Class ================= */

            CommonMaster classMaster = commonMasterRepository
                    .findById(request.getClassId())
                    .orElse(null);

            if (classMaster == null) {
                return StandardResponse.error(
                        "Invalid Class",
                        "INVALID_CLASS",
                        "classId",
                        "No class found with id " + request.getClassId());
            }

            List<ExamSubjectConfigResponse> responses = new ArrayList<>();

            /* ================= Loop Subjects ================= */

            for (SubjectMarksRequest s : request.getSubjects()) {

                Subject subject = subjectRepository
                        .findById(Long.valueOf(s.getSubjectId()))
                        .orElse(null);

                if (subject == null) {
                    return StandardResponse.error(
                            "Invalid Subject",
                            "INVALID_SUBJECT",
                            "subjectId",
                            "No subject found with id " + s.getSubjectId());
                }

                /* ================= Duplicate Check ================= */

                boolean exists = repository
                        .findBySession_IdAndExamType_IdAndSubject_IdAndClassId_IdAndIsDeleteFalse(
                                session.getId(),
                                examType.getId(),
                                subject.getId(),
                                classMaster.getId())
                        .isPresent();

                if (exists) {
                    return StandardResponse.error(
                            "Configuration already exists",
                            "DUPLICATE_CONFIG",
                            "subjectId",
                            "Already configured for subject: " + subject.getSubjectName());
                }

                /* ================= Create Config ================= */

                ExamSubjectConfig config = ExamSubjectConfig.builder()
                        .session(session)
                        .examType(examType)
                        .classId(classMaster)
                        .subject(subject)
                        .createdAt(LocalDateTime.now())
                        .build();

                repository.save(config);

                /* ================= Save Components ================= */

                List<ExamSubjectConfigComponent> components = new ArrayList<>();

                for (ComponentMarksRequest c : s.getComponents()) {

                    ExamComponentMaster component = this.componentMasterRepository
                            .findById(c.getComponentId())
                            .orElseThrow(() ->
                                    new RuntimeException("Invalid component id " + c.getComponentId()));

                    ExamSubjectConfigComponent comp = ExamSubjectConfigComponent.builder()
                            .config(config)
                            .component(component)
                            .maxMarks(c.getMaxMarks())
                            .build();

                    components.add(comp);
                }

                this.componentConfigRepository.saveAll(components);

                responses.add(map(config));
            }

            return StandardResponse.success(
                    responses,
                    "Exam subject configurations saved successfully");
        }

        /* ================= READ ================= */

        public StandardResponse<List<ExamSubjectConfigResponse>> getAll(
                        Integer sessionId,
                        Integer examTypeId,
                        Integer classId) {

                List<ExamSubjectConfigResponse> data = repository.findAllWithFilters(sessionId, examTypeId, classId)
                                .stream()
                                .map(this::map)
                                .toList();

                return StandardResponse.success(
                                data,
                                "Configurations fetched");
        }

        /* ================= UPDATE ================= */

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
                        .orElseThrow(() ->
                                new RuntimeException("Config not found for id: " + s.getId()));

                /* Clear existing components */

                config.getComponents().clear();

            } else {

                config = new ExamSubjectConfig();

                config.setSubject(
                        subjectRepository.findById(s.getSubjectId()).orElseThrow());

                config.setSession(
                        sessionRepository.findById(s.getSessionId()).orElseThrow());

                config.setExamType(
                        commonMasterRepository.findById(s.getExamTypeId()).orElseThrow());

                config.setClassId(
                        commonMasterRepository.findById(s.getClassId()).orElseThrow());
            }

            List<ExamSubjectConfigComponent> components = new ArrayList<>();

            for (ComponentConfigRequest c : s.getComponents()) {

                ExamComponentMaster component =
                        componentMasterRepository.findById(c.getComponentId())
                                .orElseThrow(() ->
                                        new RuntimeException("Invalid component id"));

                ExamSubjectConfigComponent comp =
                        ExamSubjectConfigComponent.builder()
                                .config(config)
                                .component(component)
                                .maxMarks(c.getMaxMarks())
                                .build();

                components.add(comp);
            }

            config.setComponents(components);

            repository.save(config);

            responses.add(map(config));
        }

        return StandardResponse.success(
                responses,
                "Exam subject configurations updated successfully");
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

            List<ComponentConfigResponse> components =
                    c.getComponents() == null
                            ? List.of()
                            : c.getComponents()
                            .stream()
                            .map(comp ->
                                    ComponentConfigResponse.builder()
                                            .componentId(Long.valueOf(comp.getComponent().getId()))
                                            .componentName(comp.getComponent().getComponentName())
                                            .maxMarks(comp.getMaxMarks())
                                            .build()
                            ).toList();

            return ExamSubjectConfigResponse.builder()
                    .id(c.getId())
                    .session(c.getSession() == null ? null : c.getSession().getSession())
                    .examTypeId(c.getExamType() == null ? null : c.getExamType().getId())
                    .examType(c.getExamType() == null ? null : c.getExamType().getData())
                    .classId(c.getClassId() == null ? null : c.getClassId().getId())
                    .className(c.getClassId() == null ? null : c.getClassId().getData())
                    .subjectId(Long.valueOf(c.getSubject() == null ? null : c.getSubject().getId()))
                    .subjectCode(c.getSubject() == null ? null : c.getSubject().getSubjectCode())
                    .subjectName(c.getSubject() == null ? null : c.getSubject().getSubjectName())
                    .components(components)
                    .build();
        }

}
