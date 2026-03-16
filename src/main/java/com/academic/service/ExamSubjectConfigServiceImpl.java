package com.academic.service;

import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.request.*;
import com.academic.response.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
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

    @Autowired
    private CoScholasticActivityMasterRepository coScholasticActivityMasterRepository;

    @Autowired
    private ExamCoScholasticConfigRepository coScholasticConfigRepository;

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


    /* ======================================================
       SUBJECT CONFIGURATION
       ====================================================== */

        if (request.getSubjects() != null && !request.getSubjects().isEmpty()) {

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
                        .session(session).isDelete(false)
                        .examType(examType)
                        .classId(classMaster)
                        .subject(subject)
                        .createdAt(LocalDateTime.now())
                        .build();

                repository.save(config);

                /* ================= Save Components ================= */

                List<ExamSubjectConfigComponent> components = new ArrayList<>();

                if (s.getComponents() != null) {

                    for (ComponentMarksRequest c : s.getComponents()) {

                        ExamComponentMaster component = componentMasterRepository
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

                    componentConfigRepository.saveAll(components);
                }

                responses.add(map(config));
            }
        }


    /* ======================================================
       CO-SCHOLASTIC CONFIGURATION
       ====================================================== */

        if (request.getCoScholasticActivities() != null &&
                !request.getCoScholasticActivities().isEmpty()) {

            for (CoScholasticUpdateRequest c : request.getCoScholasticActivities()) {

                CoScholasticActivityMaster activity =
                        this.coScholasticActivityMasterRepository.findById(c.getActivityId())
                                .orElseThrow(() ->
                                        new RuntimeException("Invalid activity id " + c.getActivityId()));

                boolean exists = this.coScholasticConfigRepository
                        .findBySession_IdAndExamType_IdAndClassId_IdAndActivity_IdAndIsDeleteFalse(
                                session.getId(),
                                examType.getId(),
                                classMaster.getId(),
                                activity.getId())
                        .isPresent();

                if (exists) {
                    continue;
                }

                ExamCoScholasticConfig config = ExamCoScholasticConfig.builder()
                        .session(session).isDelete(false)
                        .examType(examType)
                        .classId(classMaster)
                        .activity(activity)
                        .maxMarks(c.getMaxMarks())
                        .build();

                coScholasticConfigRepository.save(config);
            }
        }

        /* ================= SUCCESS RESPONSE ================= */

        return StandardResponse.success(
                responses,
                "Exam subject and co-scholastic configurations saved successfully");
    }
        /* ================= READ ================= */

    public StandardResponse<List<ExamSubjectConfigResponse>> getAll(
            Integer sessionId,
            Integer examTypeId,
            Integer classId) {

        /* ================= SUBJECT CONFIG ================= */

        List<ExamSubjectConfigResponse> subjectConfigs =
                repository.findAllWithFilters(sessionId, examTypeId, classId)
                        .stream()
                        .map(this::map)
                        .toList();


        /* ================= CO-SCHOLASTIC CONFIG ================= */

        List<CoScholasticConfigResponse> activities =
                coScholasticConfigRepository
                        .findAllWithFilters(sessionId, examTypeId, classId)
                        .stream()
                        .map(a -> CoScholasticConfigResponse.builder()
                                .id(Long.valueOf(a.getId()))
                                .activityId(a.getActivity().getId())
                                .activityName(a.getActivity().getActivityName())
                                .maxMarks(a.getMaxMarks())
                                .build())
                        .toList();


        /* ================= MERGE RESPONSE ================= */

        subjectConfigs.forEach(r -> r.setCoScholasticActivities(activities));


        return StandardResponse.success(
                subjectConfigs,
                "Configurations fetched");
    }


    /* ================= UPDATE ================= */

    @Transactional
    @Override
    public StandardResponse<?> updateBulk(
            ExamSubjectMarksBulkUpdateRequest request) {

        List<ExamSubjectConfigResponse> responses = new ArrayList<>();

        /* ================= SUBJECT CONFIG UPDATE ================= */

        if (request.getSubjects() != null && !request.getSubjects().isEmpty()) {

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
                            sessionRepository.findById(request.getSessionId()).orElseThrow());

                    config.setExamType(
                            commonMasterRepository.findById(request.getExamTypeId()).orElseThrow());

                    config.setClassId(
                            commonMasterRepository.findById(request.getClassId()).orElseThrow());
                }

                /* ================= ADD NEW COMPONENTS ================= */

                List<ExamSubjectConfigComponent> components = new ArrayList<>();

                for (ComponentConfigRequest c : s.getComponents()) {

                    ExamComponentMaster component =
                            componentMasterRepository.findById(c.getComponentId())
                                    .orElseThrow(() ->
                                            new RuntimeException("Invalid component id " + c.getComponentId()));

                    ExamSubjectConfigComponent comp =
                            ExamSubjectConfigComponent.builder()
                                    .config(config)
                                    .component(component)
                                    .maxMarks(c.getMaxMarks())
                                    .build();

                    components.add(comp);
                }

                /* Important: avoid replacing list reference */

                config.getComponents().addAll(components);

                repository.save(config);

                responses.add(map(config));
            }
        }


        /* ================= CO-SCHOLASTIC UPDATE ================= */

        if (request.getCoScholasticActivities() != null &&
                !request.getCoScholasticActivities().isEmpty()) {

            for (CoScholasticUpdateRequest c : request.getCoScholasticActivities()) {

                ExamCoScholasticConfig config;

                if (c.getId() != null) {

                    config = coScholasticConfigRepository.findById(Math.toIntExact(c.getId()))
                            .orElseThrow(() ->
                                    new RuntimeException("CoScholastic config not found: " + c.getId()));

                } else {

                    config = new ExamCoScholasticConfig();

                    config.setSession(
                            sessionRepository.findById(request.getSessionId()).orElseThrow());

                    config.setExamType(
                            commonMasterRepository.findById(request.getExamTypeId()).orElseThrow());

                    config.setClassId(
                            commonMasterRepository.findById(request.getClassId()).orElseThrow());
                }

                CoScholasticActivityMaster activity =
                        this.coScholasticActivityMasterRepository.findById(c.getActivityId())
                                .orElseThrow(() ->
                                        new RuntimeException("Invalid activity id " + c.getActivityId()));

                config.setActivity(activity);
                config.setMaxMarks(c.getMaxMarks());

                coScholasticConfigRepository.save(config);
            }
        }


        return StandardResponse.success(
                responses,
                "Exam subject and co-scholastic configurations updated successfully");
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


    @Override
    public StandardResponse<?> getAllComponents() {

        List<ExamComponentResponse> components =
                componentMasterRepository
                        .findByActiveTrueOrderByDisplayOrder()
                        .stream()
                        .map(c -> ExamComponentResponse.builder()
                                .id(Long.valueOf(c.getId()))
                                .componentName(c.getComponentName())
                                .displayOrder(c.getDisplayOrder())
                                .build())
                        .toList();

        return StandardResponse.success(
                components,
                "Components fetched successfully");
    }

    @Override
    public StandardResponse<?> getActivityMasters() {

        List<CoScholasticActivityResponse> activities =
                this.coScholasticActivityMasterRepository.findByIsActiveTrueOrderByDisplayOrder()
                        .stream()
                        .map(a -> CoScholasticActivityResponse.builder()
                                .id(a.getId())
                                .activityName(a.getActivityName())
                                .displayOrder(a.getDisplayOrder())
                                .build())
                        .toList();

        return StandardResponse.success(
                activities,
                "Co-Scholastic activities fetched successfully");
    }

}
