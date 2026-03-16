package com.academic.service;

import com.academic.dto.ComponentMarksResponse;
import com.academic.dto.MarksheetRequest;
import com.academic.entity.*;
import com.academic.exception.ResourceNotFoundException;
import com.academic.repository.*;
import com.academic.request.CoScholasticMarksRequest;
import com.academic.request.ComponentMarksRequest;
import com.academic.request.SubjectMarksRequest;
import com.academic.response.MarksheetDetailResponse;
import com.academic.response.MarksheetResponse;
import com.academic.response.StandardResponse;
import com.academic.response.SubjectMarksResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarksheetServiceImpl implements MarksheetService {

    @Autowired
    private final MarksheetRepository marksheetRepo;
//    private final MarksheetSubjectMarksRepository subjectRepo;
    @Autowired
    private final CommonMasterRepository commonRepo;
    @Autowired
    private final SessionRepository sessionRepo;
    @Autowired
    private final StudentRepository studentRepository;
    @Autowired
    private final SubjectRepository subjectMasterRepo;
    @Autowired
    private final TemplateEngine templateEngine;
    @Autowired
    private ExamComponentMasterRepostory componentRepo;

    @Autowired
    private ExamSubjectConfigRepository examSubjectConfigRepository;

    @Autowired
    private ExamSubjectConfigComponentRepository examSubjectConfigComponentRepository;

    @Autowired
    private CoScholasticActivityMasterRepository coScholasticActivityMasterRepository;

    @Autowired
    private ExamCoScholasticConfigRepository examCoScholasticConfigRepository;

    @Transactional
    @Override
    public StandardResponse<?> saveMarksheet(MarksheetRequest request) {

        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            return StandardResponse.error(
                    "Subjects are required",
                    "NO_SUBJECTS",
                    "subjects",
                    "At least one subject is mandatory");
        }

        /* ================= CREATE MARKSHEET ================= */

        Marksheet sheet = new Marksheet();

        sheet.setStudentId(request.getStudentId());
        sheet.setClassId(request.getClassId());
        sheet.setSectionId(request.getSectionId());
        sheet.setSessionId(request.getSessionId());
        sheet.setExamTypeId(request.getExamTypeId());
        sheet.setExamDate(request.getExamDate());

        sheet.setTotalMarksObtained(request.getTotalMarksObtained());
        sheet.setTotalMaxMarks(request.getTotalMaxMarks());
        sheet.setPercentage(request.getPercentage());
        sheet.setGrade(request.getGrade());

        /* ================= SUBJECTS ================= */

        List<MarksheetSubject> subjects = new ArrayList<>();

        for (SubjectMarksRequest s : request.getSubjects()) {

            /* Load subject config */

            ExamSubjectConfig config =
                    (ExamSubjectConfig) this.examSubjectConfigRepository
                            .findBySession_IdAndExamType_IdAndSubject_IdAndClassId_IdAndIsDeleteFalse(
                                    request.getSessionId(),
                                    request.getExamTypeId(),
                                    s.getSubjectId(),
                                    request.getClassId())
                            .orElseThrow(() ->
                                    new RuntimeException("Subject config not found"));

            MarksheetSubject subject = new MarksheetSubject();

            subject.setMarksheet(sheet);
            subject.setSubjectId(s.getSubjectId());
            subject.setSubjectRemarks(s.getSubjectRemarks());

            List<MarksheetSubjectComponent> components = new ArrayList<>();

            int total = 0;
            int totalMax = 0;

            for (ComponentMarksRequest c : s.getComponents()) {

                ExamComponentMaster component =
                        componentRepo.findById(c.getComponentId())
                                .orElseThrow(() ->
                                        new RuntimeException("Component not found"));

                /* Get max marks from configuration */

                ExamSubjectConfigComponent configComponent =
                        this.examSubjectConfigComponentRepository
                                .findByConfig_IdAndComponent_Id(config.getId(), component.getId());

                MarksheetSubjectComponent comp = new MarksheetSubjectComponent();

                comp.setSubject(subject);
                comp.setComponent(component);
                comp.setMarksObtained(c.getMarksObtained());
                comp.setMaxMarks(configComponent.getMaxMarks());

                total += safe(c.getMarksObtained());
                totalMax += safe(configComponent.getMaxMarks());

                components.add(comp);
            }

            subject.setTotalMarks(total);
            subject.setTotalMax(totalMax);
            subject.setGrade(calculateGrade(total));

            subject.setComponents(components);

            subjects.add(subject);
        }

        sheet.setSubjects(subjects);

        /* ================= CO-SCHOLASTIC ================= */

        if (request.getCoScholasticActivities() != null &&
                !request.getCoScholasticActivities().isEmpty()) {

            List<MarksheetCoScholastic> activities = new ArrayList<>();

            for (CoScholasticMarksRequest c : request.getCoScholasticActivities()) {

                CoScholasticActivityMaster activity =
                        coScholasticActivityMasterRepository
                                .findById(c.getActivityId())
                                .orElseThrow(() -> new RuntimeException("Activity not found"));

                /* get configured max marks */

                ExamCoScholasticConfig config =
                        (ExamCoScholasticConfig) this.examCoScholasticConfigRepository
                                .findBySession_IdAndExamType_IdAndClassId_IdAndActivity_IdAndIsDeleteFalse(
                                        request.getSessionId(),
                                        request.getExamTypeId(),
                                        request.getClassId(),
                                        activity.getId())
                                .orElseThrow(() -> new RuntimeException("Activity config not found"));

                MarksheetCoScholastic a = new MarksheetCoScholastic();

                a.setMarksheet(sheet);
                a.setActivity(activity);

                a.setMarksObtained(c.getMarksObtained());
                a.setMaxMarks(config.getMaxMarks());

                a.setGrade(calculateGrade(c.getMarksObtained()));

                activities.add(a);
            }

            sheet.setCoScholasticActivities(activities);
        }

        /* ================= SAVE ================= */

        marksheetRepo.save(sheet);

        return StandardResponse.success(
                sheet.getId(),
                "Marksheet created successfully");
    }

    private String calculateGrade(double marks) {

        if (marks >= 90) return "A1";
        if (marks >= 80) return "A2";
        if (marks >= 70) return "B1";
        if (marks >= 60) return "B2";
        if (marks >= 50) return "C1";
        if (marks >= 40) return "C2";

        return "F";
    }

    @Override
    @Transactional
    public StandardResponse<?> update(Long id, MarksheetRequest request) {

        if (id == null) {
            return StandardResponse.error(
                    "Invalid request",
                    "INVALID_ID",
                    "id",
                    "Marksheet id is required");
        }

        Marksheet sheet = marksheetRepo.findById(id).orElse(null);

        if (sheet == null || Boolean.TRUE.equals(sheet.getIsDeleted())) {
            return StandardResponse.error(
                    "Marksheet not found",
                    "NOT_FOUND",
                    "id",
                    "Invalid marksheet id");
        }

        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            return StandardResponse.error(
                    "Subjects are required",
                    "NO_SUBJECTS",
                    "subjects",
                    "At least one subject is mandatory");
        }

        /* BASIC DETAILS */

        sheet.setStudentId(request.getStudentId());
        sheet.setClassId(request.getClassId());
        sheet.setSectionId(request.getSectionId());
        sheet.setSessionId(request.getSessionId());

        sheet.setExamTypeId(request.getExamTypeId());

        sheet.setExamDate(request.getExamDate());

        sheet.setTotalMarksObtained(request.getTotalMarksObtained());
        sheet.setTotalMaxMarks(request.getTotalMaxMarks());
        sheet.setPercentage(request.getPercentage());
        sheet.setGrade(request.getGrade());

        sheet.setAttendanceDays(request.getAttendanceDays());
        sheet.setTotalWorkingDays(request.getTotalWorkingDays());

        sheet.setConductGrade(request.getConductGrade());
        sheet.setSportsGrade(request.getSportsGrade());
        sheet.setExtraCurricularGrade(request.getExtraCurricularGrade());

        /* RESET SUBJECTS (CASCADE DELETE) */

        sheet.getSubjects().clear();

        List<MarksheetSubject> subjects = new ArrayList<>();

        for (SubjectMarksRequest s : request.getSubjects()) {

            MarksheetSubject subject = new MarksheetSubject();

            subject.setMarksheet(sheet);
            subject.setSubjectId(s.getSubjectId());
            subject.setSubjectRemarks(s.getSubjectRemarks());

            List<MarksheetSubjectComponent> components = new ArrayList<>();

            int total = 0;
            int totalMax = 0;

            for (ComponentMarksRequest c : s.getComponents()) {

                ExamComponentMaster component =
                        componentRepo.findById(c.getComponentId())
                                .orElseThrow(() -> new RuntimeException("Component not found"));

                MarksheetSubjectComponent comp = new MarksheetSubjectComponent();

                comp.setSubject(subject);
                comp.setComponent(component);
                comp.setMarksObtained(c.getMarksObtained());
                comp.setMaxMarks(c.getMaxMarks());

                total += safe(c.getMarksObtained());
                totalMax += safe(c.getMaxMarks());

                components.add(comp);
            }

            subject.setTotalMarks(total);
            subject.setTotalMax(totalMax);
            subject.setGrade(calculateGrade(total));
            subject.setComponents(components);

            subjects.add(subject);
        }

        sheet.setSubjects(subjects);

        marksheetRepo.save(sheet);

        return StandardResponse.success(
                sheet.getId(),
                "Marksheet updated successfully");
    }
    

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }

    /* GET ALL (PAGINATION + FILTERS) */
    public StandardResponse<?> getAll(
            Integer classId,
            Integer examTypeId,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Marksheet> result = (classId != null && examTypeId != null)
                ? marksheetRepo.findByClassIdAndExamTypeIdAndIsDeletedFalse(
                        classId, examTypeId, pageable)
                : marksheetRepo.findByIsDeletedFalse(pageable);

        List<MarksheetResponse> dtoList = result.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return StandardResponse.success(
                dtoList,
                "Marksheets fetched",
                StandardResponse.ResponseMetadata.builder()
                        .totalRecords(result.getTotalElements())
                        .totalPages(result.getTotalPages())
                        .currentPage(page)
                        .pageSize(size)
                        .build());
    }

    @Override
    public StandardResponse<?> getById(Long id) {

        if (id == null) {
            return StandardResponse.error(
                    "Invalid request",
                    "INVALID_ID",
                    "id",
                    "Marksheet id is required");
        }

        Marksheet sheet = marksheetRepo.findByIdAndIsDeletedFalse(id);

        if (sheet == null || Boolean.TRUE.equals(sheet.getIsDeleted())) {
            return StandardResponse.error(
                    "Marksheet not found",
                    "NOT_FOUND",
                    "id",
                    "Invalid marksheet id");
        }

        MarksheetDetailResponse response = mapToDetailResponse(sheet);

        return StandardResponse.success(response, "Marksheet fetched successfully");
    }


    private MarksheetResponse mapToResponse(Marksheet sheet) {
        Student student = studentRepository
                .findById(sheet.getStudentId() != null ? sheet.getStudentId().intValue() : -1).get();
        return MarksheetResponse.builder()
                .id(sheet.getId())
                .studentId(sheet.getStudentId())
                .studentName(student.getFirstName() + " " + student.getLastName()) // placeholder if no student repo
                .className(getName(sheet.getClassId()))
                .sectionName(getName(sheet.getSectionId()))
                .sessionName(getSessionName(sheet.getSessionId()))
                .examTypeName(getName(sheet.getExamTypeId()))
                .examDate(sheet.getExamDate())
                .totalMarksObtained(sheet.getTotalMarksObtained())
                .totalMaxMarks(sheet.getTotalMaxMarks())
                .percentage(sheet.getPercentage())
                .grade(sheet.getGrade())
                .published(sheet.getPublished())
                .build();
    }

    private MarksheetDetailResponse mapToDetailResponse(Marksheet sheet) {

        Student student = studentRepository
                .findById(sheet.getStudentId() != null ? sheet.getStudentId().intValue() : -1)
                .orElse(null);

        List<SubjectMarksResponse> subjectResponses = sheet.getSubjects()
                .stream()
                .map(subject -> {

                    List<ComponentMarksResponse> components =
                            subject.getComponents()
                                    .stream()
                                    .map(c -> {

                                        ComponentMarksResponse comp = new ComponentMarksResponse();

                                        comp.setComponentId(c.getComponent().getId());
                                        comp.setComponentName(c.getComponent().getComponentName());
                                        comp.setMarksObtained(c.getMarksObtained());
                                        comp.setMaxMarks(c.getMaxMarks());

                                        return comp;

                                    }).toList();

                    SubjectMarksResponse s = new SubjectMarksResponse();

                    s.setId(subject.getId());
                    s.setSubjectId(subject.getSubjectId());
                    s.setSubjectName(getSubjectName(subject.getSubjectId()));
                    s.setTotalMarks(subject.getTotalMarks());
                    s.setTotalMax(subject.getTotalMax());
                    s.setGrade(subject.getGrade());
                    s.setSubjectRemarks(subject.getSubjectRemarks());
                    s.setComponents(components);

                    return s;

                }).toList();


        MarksheetDetailResponse response = new MarksheetDetailResponse();

        response.setId(sheet.getId());

        /* STUDENT INFO */

        response.setStudentId(sheet.getStudentId());
        response.setStudentName(student != null
                ? student.getFirstName() + " " + student.getLastName()
                : null);

        response.setClassId(sheet.getClassId());
        response.setClassName(getName(sheet.getClassId()));

        response.setSectionId(sheet.getSectionId());
        response.setSectionName(getName(sheet.getSectionId()));

        response.setSessionId(sheet.getSessionId());
        response.setSessionName(getSessionName(sheet.getSessionId()));

        /* EXAM */

        response.setExamTypeId(sheet.getExamTypeId());
        response.setExamTypeName(getName(sheet.getExamTypeId()));

        response.setExamDate(sheet.getExamDate());

        /* SUBJECTS */

        response.setSubjects(subjectResponses);

        /* SUMMARY */

        response.setTotalMarksObtained(sheet.getTotalMarksObtained());
        response.setTotalMaxMarks(sheet.getTotalMaxMarks());
        response.setPercentage(sheet.getPercentage());
        response.setGrade(sheet.getGrade());

        /* EVALUATION */

        response.setAttendanceDays(sheet.getAttendanceDays());
        response.setTotalWorkingDays(sheet.getTotalWorkingDays());
        response.setConductGrade(sheet.getConductGrade());
        response.setSportsGrade(sheet.getSportsGrade());
        response.setExtraCurricularGrade(sheet.getExtraCurricularGrade());

        response.setPublished(sheet.getPublished());

        return response;
    }
    private String getName(Integer id) {
        if (id == null)
            return null;
        return commonRepo.findByIdAndStatusTrue(id)
                .map(cm -> cm.getData())
                .orElse("Unknown (" + id + ")");
    }

    private String getSessionName(Integer id) {
        if (id == null)
            return null;
        return sessionRepo.findById(id)
                .map(s -> s.getSession())
                .orElse("Unknown (" + id + ")");
    }

    private String getSubjectName(Integer id) {
        if (id == null)
            return null;
        return subjectMasterRepo.findByIdAndIsDeletedFalse(id)
                .map(s -> s.getSubjectName())
                .orElse("Unknown (" + id + ")");
    }

    public StandardResponse<?> delete(Long id) {
        return marksheetRepo.findById(id).map(m -> {
            m.setIsDeleted(true);
            marksheetRepo.save(m);
            return StandardResponse.success("Marksheet deleted");
        }).orElse(StandardResponse.error(
                "Marksheet not found",
                "NOT_FOUND",
                "id",
                "Invalid marksheet id"));
    }


    @Override
    public byte[] generateMarksheetPdf(Long id) {

        log.info("Generating marksheet PDF for id {}", id);

        Marksheet sheet = marksheetRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid marksheet id"));

        if (Boolean.TRUE.equals(sheet.getIsDeleted())) {
            throw new ResourceNotFoundException("Marksheet deleted");
        }

        MarksheetDetailResponse response = mapToDetailResponse(sheet);

        try {

            String html = generateHtml(response);

            return convertHtmlToPdf(html);

        } catch (Exception e) {

            log.error("Error generating PDF for marksheet {}", id, e);

            throw new RuntimeException("PDF generation failed");
        }

    }


    private String generateHtml(MarksheetDetailResponse data) {

        Context context = new Context();

        context.setVariable("student", data);
        context.setVariable("subjects", data.getSubjects());

        return templateEngine.process("marksheet-template", context);
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }

}
