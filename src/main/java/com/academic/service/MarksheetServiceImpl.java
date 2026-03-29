package com.academic.service;

import com.academic.dto.ComponentMarksResponse;
import com.academic.dto.MarksheetRequest;
import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.request.CoScholasticMarksRequest;
import com.academic.request.ComponentMarksRequest;
import com.academic.request.SubjectMarksRequest;
import com.academic.response.*;
import com.academic.utility.Template;
import com.academic.exception.ResourceNotFoundException;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


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

            ExamSubjectConfig config = (ExamSubjectConfig) this.examSubjectConfigRepository
                    .findBySession_IdAndExamType_IdAndSubject_IdAndClassId_IdAndIsDeleteFalse(
                            request.getSessionId(),
                            request.getExamTypeId(),
                            s.getSubjectId(),
                            request.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject config not found"));

            MarksheetSubject subject = new MarksheetSubject();

            subject.setMarksheet(sheet);
            subject.setSubjectId(s.getSubjectId());
            subject.setSubjectRemarks(s.getSubjectRemarks());

            List<MarksheetSubjectComponent> components = new ArrayList<>();

            int total = 0;
            int totalMax = 0;

            for (ComponentMarksRequest c : s.getComponents()) {

                ExamComponentMaster component = componentRepo.findById(c.getComponentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

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

                CoScholasticActivityMaster activity = coScholasticActivityMasterRepository
                        .findById(c.getActivityId())
                        .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

                /* get configured max marks */

                ExamCoScholasticConfig config =
                        (ExamCoScholasticConfig) this.examCoScholasticConfigRepository
                                .findBySession_IdAndExamType_IdAndClassId_IdAndActivity_IdAndIsDeleteFalse(
                                        request.getSessionId(),
                                        request.getExamTypeId(),
                                        request.getClassId(),
                                        activity.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Activity config not found"));

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

    private String calculateGrade(double percentage) {
        if (percentage >= 91) return "A1";
        if (percentage >= 81) return "A2";
        if (percentage >= 71) return "B1";
        if (percentage >= 61) return "B2";
        if (percentage >= 51) return "C1";
        if (percentage >= 41) return "C2";
        if (percentage >= 33) return "D";
        return "E (Needs Improvement)";
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

        /* ================= RESET SUBJECTS ================= */

        sheet.getSubjects().clear();

        List<MarksheetSubject> subjects = new ArrayList<>();

        for (SubjectMarksRequest s : request.getSubjects()) {

            /* Load subject configuration */

            ExamSubjectConfig config =
                    (ExamSubjectConfig) examSubjectConfigRepository
                            .findBySession_IdAndExamType_IdAndSubject_IdAndClassId_IdAndIsDeleteFalse(
                                    request.getSessionId(),
                                    request.getExamTypeId(),
                                    s.getSubjectId(),
                                    request.getClassId())
                            .orElseThrow(() -> new ResourceNotFoundException("Subject config not found"));

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
                                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

                /* get configured max marks */

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

        sheet.getSubjects().addAll(subjects);

        /* ================= RESET CO-SCHOLASTIC ================= */

        sheet.getCoScholasticActivities().clear();

        if (request.getCoScholasticActivities() != null &&
                !request.getCoScholasticActivities().isEmpty()) {

            List<MarksheetCoScholastic> activities = new ArrayList<>();

            for (CoScholasticMarksRequest c : request.getCoScholasticActivities()) {

                CoScholasticActivityMaster activity = coScholasticActivityMasterRepository
                        .findById(c.getActivityId())
                        .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

                ExamCoScholasticConfig config =
                        (ExamCoScholasticConfig) examCoScholasticConfigRepository
                                .findBySession_IdAndExamType_IdAndClassId_IdAndActivity_IdAndIsDeleteFalse(
                                        request.getSessionId(),
                                        request.getExamTypeId(),
                                        request.getClassId(),
                                        activity.getId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException("Activity config not found"));

                MarksheetCoScholastic a = new MarksheetCoScholastic();

                a.setMarksheet(sheet);
                a.setActivity(activity);
                a.setMarksObtained(c.getMarksObtained());
                a.setMaxMarks(config.getMaxMarks());
                a.setGrade(calculateGrade(c.getMarksObtained()));

                activities.add(a);
            }

            sheet.getCoScholasticActivities().addAll(activities);
        }

        marksheetRepo.save(sheet);

        return StandardResponse.success(
                sheet.getId(),
                "Marksheet updated successfully");
    }



    /* GET ALL (PAGINATION + FILTERS) */
    public StandardResponse<?> getAll(Integer classId, Integer examTypeId, int page, int size,Integer sessionId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Marksheet> result = (classId != null && examTypeId != null)
                ? marksheetRepo.findByClassIdAndExamTypeIdAndSessionIdAndIsDeletedFalse(
                        classId, examTypeId,sessionId ,pageable)
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

        if (sheet == null) {
            return StandardResponse.error(
                    "Marksheet not found",
                    "NOT_FOUND",
                    "id",
                    "Invalid marksheet id");
        }

        MarksheetDetailResponse response = mapToDetailResponse(sheet);

        return StandardResponse.success(
                response,
                "Marksheet fetched successfully");
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

        if (sheet == null) {
            return null;
        }

        Student student = studentRepository
                .findById(sheet.getStudentId() != null ? sheet.getStudentId() : -1)
                .orElse(null);

        /* ================= SUBJECTS ================= */

        List<SubjectMarksResponse> subjectResponses =
                sheet.getSubjects()
                        .stream()
                        .map(subject -> {

                            List<ComponentMarksResponse> components =
                                    subject.getComponents()
                                            .stream()
                                            .map(c -> {

                                                ComponentMarksResponse comp =
                                                        new ComponentMarksResponse();

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


        /* ================= CO SCHOLASTIC ================= */

        List<CoScholasticResponse> activities =
                sheet.getCoScholasticActivities()
                        .stream()
                        .map(a -> {

                            CoScholasticResponse r = new CoScholasticResponse();

                            r.setActivityId(a.getActivity().getId());
                            r.setActivityName(a.getActivity().getActivityName());
                            r.setMarksObtained(a.getMarksObtained());
                            r.setMaxMarks(a.getMaxMarks());
                            r.setGrade(a.getGrade());

                            return r;

                        }).toList();


        /* ================= RESPONSE ================= */

        MarksheetDetailResponse response = new MarksheetDetailResponse();

        response.setId(sheet.getId());

        /* STUDENT */

        response.setStudentId(sheet.getStudentId());

        response.setStudentName(
                student != null
                        ? student.getFirstName() + " " + (student.getMiddleName() != null ? student.getMiddleName() + " " : "") + student.getLastName()
                        : null);

        response.setAdmissionNo(student != null ? student.getAdmissionNo() : null);
        response.setFatherName(student != null ? student.getFatherName() : null);
        response.setMotherName(student != null ? student.getMotherName() : null);
        response.setDob(student != null ? student.getDateOfBirth() : null);

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

        /* CO SCHOLASTIC */

        response.setCoScholasticActivities(activities);

        /* SUMMARY */

        response.setTotalMarksObtained(sheet.getTotalMarksObtained());
        response.setTotalMaxMarks(sheet.getTotalMaxMarks());
        response.setPercentage(sheet.getPercentage());
        response.setGrade(sheet.getGrade());
        response.setGpa(sheet.getCgpa());

        /* EVALUATION */

        response.setAttendanceDays(sheet.getAttendanceDays());
        response.setTotalWorkingDays(sheet.getTotalWorkingDays());

        response.setConductGrade(sheet.getConductGrade());
        response.setSportsGrade(sheet.getSportsGrade());
        response.setExtraCurricularGrade(sheet.getExtraCurricularGrade());

        response.setTeacherRemarks(sheet.getTeacherRemarks());
        response.setPrincipalRemarks(sheet.getPrincipalRemarks());

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


    @Transactional(readOnly = true)
    public byte[] generateMarksheetPdf(Long studentId,
                                       Integer sessionId,
                                       String type,
                                       Integer examTypeId) {

        String requestType = type == null ? "ANNUAL" : type.toUpperCase();

        String html;

        if ("TERM".equals(requestType)) {

            Marksheet sheet = marksheetRepo
                    .findByStudentIdAndSessionIdAndExamTypeIdAndIsDeletedFalse(
                            studentId, sessionId, examTypeId);

            MarksheetDetailResponse data = mapToDetailResponse(sheet);

            html = buildTermHtml(data);

        } else {

            Integer term1Id = getExamTypeId("TERM 1");
            Integer term2Id = getExamTypeId("TERM 2");

            MarksheetDetailResponse t1 = mapToDetailResponse(
                    marksheetRepo.findByStudentIdAndSessionIdAndExamTypeIdAndIsDeletedFalse(studentId, sessionId, term1Id)
            );

            MarksheetDetailResponse t2 = mapToDetailResponse(
                    marksheetRepo.findByStudentIdAndSessionIdAndExamTypeIdAndIsDeletedFalse(studentId, sessionId, term2Id)
            );

            if (t1 == null && t2 == null) {
                throw new ResourceNotFoundException("No marksheet data found for Term 1 or Term 2 for student " + studentId);
            }

            html = buildAnnualHtml(t1, t2);
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            String baseUri = getClass().getClassLoader()
                    .getResource("templates/").toExternalForm();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(output);
            builder.run();

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private Integer getExamTypeId(String name) {

        CommonMaster cm = commonRepo
                .findByCommonMasterKeyAndDataAndStatusTrue("EXAM_TYPE", name);

        if (cm == null) {
            throw new ResourceNotFoundException(name + " not configured");
        }

        return cm.getId();
    }


    private String buildTermHtml(MarksheetDetailResponse data) {

        String html = Template.TERM_MARKSHEET_HTML;

        html = patchCommon(html, data);

        html = html.replace("${SUBJECT_ROWS}", buildTermRows(data.getSubjects()));
        html = html.replace("${TOTAL_MARKS}", String.valueOf(data.getTotalMarksObtained()));
        html = html.replace("${TOTAL_MAX}", String.valueOf(data.getTotalMaxMarks()));
        html = html.replace("${PERCENTAGE}", String.valueOf(data.getPercentage()));
        html = html.replace("${GRADE}", safe(data.getGrade()));
        html = html.replace("${ACTIVITY_ROWS}", buildCoSingle(data.getCoScholasticActivities()));
        html = html.replace("${DATE}", data.getExamDate() != null ? data.getExamDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
        html = html.replace("${REPORT_TITLE}", "TERM REPORT");

        return html;
    }

    private String buildAnnualHtml(MarksheetDetailResponse t1,
                                   MarksheetDetailResponse t2) {

        String html = Template.ANNUAL_MARKSHEET_HTML;

        MarksheetDetailResponse base = (t1 != null) ? t1 : t2;
        html = patchCommon(html, base);

        html = html.replace("${SUBJECT_ROWS}", buildAnnualRows(t1, t2));

        int t1Marks = (t1 != null) ? safe(t1.getTotalMarksObtained()) : 0;
        int t2Marks = (t2 != null) ? safe(t2.getTotalMarksObtained()) : 0;
        int t1Max = (t1 != null) ? safe(t1.getTotalMaxMarks()) : 0;
        int t2Max = (t2 != null) ? safe(t2.getTotalMaxMarks()) : 0;

        int total = t1Marks + t2Marks;
        int max = t1Max + t2Max;

        html = html.replace("${TOTAL_MARKS}", String.valueOf(total));
        html = html.replace("${TOTAL_MAX}", String.valueOf(max));
        double percentage = (max > 0) ? (total * 100.0 / max) : 0;
        html = html.replace("${PERCENTAGE}", String.format("%.2f", percentage));
        html = html.replace("${GRADE}", calculateGrade(percentage));
        html = html.replace("${ACTIVITY_ROWS}", buildCoDual(
                t1 != null ? t1.getCoScholasticActivities() : null,
                t2 != null ? t2.getCoScholasticActivities() : null));
        html = html.replace("${DATE}", LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        html = html.replace("${PROMOTED_TO}", "__________"); // Placeholder for promotion field

        return html;
    }


    private String buildCoSingle(List<CoScholasticResponse> list) {

        StringBuilder rows = new StringBuilder();

        if (list == null || list.isEmpty()) {
            return "";
        }

        for (CoScholasticResponse a : list) {

            if (a == null) continue;

            rows.append("<tr>")
                    .append("<td>").append(safe(a.getActivityName())).append("</td>")
                    .append("<td>").append(safe(a.getGrade())).append("</td>")
                    .append("</tr>");
        }

        return rows.toString();
    }

    private String buildCoDual(List<CoScholasticResponse> t1,
                               List<CoScholasticResponse> t2) {

        StringBuilder rows = new StringBuilder();

        int size1 = (t1 != null) ? t1.size() : 0;
        int size2 = (t2 != null) ? t2.size() : 0;

        int max = Math.max(size1, size2);

        for (int i = 0; i < max; i++) {

            CoScholasticResponse a1 = (t1 != null && i < size1) ? t1.get(i) : null;
            CoScholasticResponse a2 = (t2 != null && i < size2) ? t2.get(i) : null;

            rows.append("<tr>");

            // TERM 1
            rows.append("<td>")
                    .append(a1 != null ? safe(a1.getActivityName()) : "")
                    .append("</td>");

            rows.append("<td>")
                    .append(a1 != null ? safe(a1.getGrade()) : "")
                    .append("</td>");

            // TERM 2
            rows.append("<td>")
                    .append(a2 != null ? safe(a2.getActivityName()) : "")
                    .append("</td>");

            rows.append("<td>")
                    .append(a2 != null ? safe(a2.getGrade()) : "")
                    .append("</td>");

            rows.append("</tr>");
        }

        return rows.toString();
    }


    private String patchCommon(String html, MarksheetDetailResponse d) {

        return html
                .replace("${LEFT_LOGO}", "left_logo.png")
                .replace("${RIGHT_LOGO}", "right_logo.png")
                .replace("${SCHOOL_NAME}", "PROGRESSIVE PUBLIC SCHOOL (PPS)")
                .replace("${SESSION}", safe(d.getSessionName()))
                .replace("${STUDENT_NAME}", safe(d.getStudentName()))
                .replace("${CLASS}", safe(d.getClassName()))
                .replace("${SECTION}", safe(d.getSectionName()))
                .replace("${ROLL_NO}", d.getStudentId() != null ? String.valueOf(d.getStudentId()) : "")
                .replace("${ADMISSION_NO}", safe(d.getAdmissionNo()))
                .replace("${FATHER_NAME}", safe(d.getFatherName()))
                .replace("${MOTHER_NAME}", safe(d.getMotherName()))
                .replace("${DOB}", d.getDob() != null ? d.getDob().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "");
    }




    private String buildTermRows(List<SubjectMarksResponse> subjects) {

        StringBuilder rows = new StringBuilder();

        for (SubjectMarksResponse s : subjects) {

            int pt=0, nb=0, se=0, term=0;

            for (ComponentMarksResponse c : s.getComponents()) {

                String name = c.getComponentName().toUpperCase();
                if (name.contains("PERIODIC") || name.contains("PT")) pt = safe(c.getMarksObtained());
                else if (name.contains("NOTEBOOK") || name.contains("NB")) nb = safe(c.getMarksObtained());
                else if (name.contains("ENRICHMENT") || name.contains("SE")) se = safe(c.getMarksObtained());
                else if (name.contains("TERM")) term = safe(c.getMarksObtained());
            }

            int total = pt + nb + se + term;

            rows.append("<tr>")
                    .append("<td style='text-align:left; padding-left:10px;'>").append(s.getSubjectName()).append("</td>")
                    .append("<td>").append(pt).append("</td>")
                    .append("<td>").append(nb).append("</td>")
                    .append("<td>").append(se).append("</td>")
                    .append("<td>").append(term).append("</td>")
                    .append("<td style='background-color: #f3f4f6; font-weight:bold;'>").append(total).append("</td>")
                    .append("<td>").append(s.getGrade()).append("</td>")
                    .append("</tr>");
        }

        return rows.toString();
    }

    private String buildAnnualRows(MarksheetDetailResponse t1,
                                   MarksheetDetailResponse t2) {

        Map<Integer, SubjectMarksResponse> map1 = (t1 != null && t1.getSubjects() != null)
                ? t1.getSubjects().stream().collect(Collectors.toMap(SubjectMarksResponse::getSubjectId, s -> s))
                : Collections.emptyMap();

        Map<Integer, SubjectMarksResponse> map2 = (t2 != null && t2.getSubjects() != null)
                ? t2.getSubjects().stream().collect(Collectors.toMap(SubjectMarksResponse::getSubjectId, s -> s))
                : Collections.emptyMap();

        Set<Integer> all = new HashSet<>();
        all.addAll(map1.keySet());
        all.addAll(map2.keySet());

        StringBuilder rows = new StringBuilder();

        for (Integer id : all) {

            SubjectMarksResponse s1 = map1.get(id);
            SubjectMarksResponse s2 = map2.get(id);

            rows.append("<tr>");
            rows.append("<td style='text-align:left; padding-left:10px;'>").append(s1 != null ? s1.getSubjectName() : s2.getSubjectName()).append("</td>");

            appendComponents(rows, s1);
            appendComponents(rows, s2);

            double m1 = s1 != null ? safe(s1.getTotalMarks()) : 0;
            double x1 = s1 != null ? safe(s1.getTotalMax()) : 100;
            double m2 = s2 != null ? safe(s2.getTotalMarks()) : 0;
            double x2 = s2 != null ? safe(s2.getTotalMax()) : 100;

            // GRAND TOTAL = T1(50%) + T2(50%)
            // Handle cases where one term might be missing to avoid penalizing the score
            double gradTotal;
            if (s1 != null && s2 != null) {
                gradTotal = ((m1 * 100.0 / x1) + (m2 * 100.0 / x2)) / 2.0;
            } else if (s1 != null) {
                gradTotal = (m1 * 100.0 / x1);
            } else if (s2 != null) {
                gradTotal = (m2 * 100.0 / x2);
            } else {
                gradTotal = 0;
            }
            
            long roundedGradTotal = Math.round(gradTotal);

            rows.append("<td style='font-weight:bold;'>").append(roundedGradTotal).append("</td>");
            rows.append("<td style='font-weight:bold;'>").append(calculateGrade(gradTotal)).append("</td>");

            rows.append("</tr>");
        }

        return rows.toString();
    }

    private String buildCoScholasticSingle(List<CoScholasticResponse> list) {

        StringBuilder rows = new StringBuilder();

        for (CoScholasticResponse a : list) {
            rows.append("<tr>")
                    .append("<td>").append(a.getActivityName()).append("</td>")
                    .append("<td>").append(a.getGrade()).append("</td>")
                    .append("</tr>");
        }

        return rows.toString();
    }


    private String commonHeader(String html, MarksheetDetailResponse data) {

        html = html.replace("${LEFT_LOGO}", "left_logo.png");
        html = html.replace("${RIGHT_LOGO}", "right_logo.png");

        html = html.replace("${SCHOOL_NAME}", "PROGRESSIVE PUBLIC SCHOOL (PPS)");
        html = html.replace("${SESSION}", safe(data.getSessionName()));

        html = html.replace("${STUDENT_NAME}", safe(data.getStudentName()));
        html = html.replace("${CLASS}", safe(data.getClassName()));
        html = html.replace("${SECTION}", safe(data.getSectionName()));
        html = html.replace("${ROLL_NO}", String.valueOf(data.getStudentId()));
        html = html.replace("${ADMISSION_NO}", String.valueOf(data.getStudentId()));

        html = html.replace("${FATHER_NAME}", "FATHER NAME");
        html = html.replace("${MOTHER_NAME}", "MOTHER NAME");
        html = html.replace("${DOB}", "01/01/2010");

        return html;
    }

    private void appendComponents(StringBuilder rows, SubjectMarksResponse s) {

        // ✅ Handle NULL subject (important for ANNUAL merge)
        if (s == null) {
            rows.append("<td></td>"); // PT
            rows.append("<td></td>"); // NB
            rows.append("<td></td>"); // SE
            rows.append("<td></td>"); // TERM
            rows.append("<td></td>"); // TOTAL
            rows.append("<td></td>"); // GRADE
            return;
        }

        int pt = 0;
        int nb = 0;
        int se = 0;
        int term = 0;

        // ✅ Handle NULL components
        if (s.getComponents() != null) {

            for (ComponentMarksResponse c : s.getComponents()) {

                if (c == null || c.getComponentName() == null) continue;

                String name = c.getComponentName().toUpperCase();
                if (name.contains("PERIODIC") || name.contains("PT")) pt = safe(c.getMarksObtained());
                else if (name.contains("NOTEBOOK") || name.contains("NB")) nb = safe(c.getMarksObtained());
                else if (name.contains("ENRICHMENT") || name.contains("SE")) se = safe(c.getMarksObtained());
                else if (name.contains("TERM")) term = safe(c.getMarksObtained());
            }
        }

        int total = pt + nb + se + term;

        rows.append("<td>").append(pt).append("</td>");
        rows.append("<td>").append(nb).append("</td>");
        rows.append("<td>").append(se).append("</td>");
        rows.append("<td>").append(term).append("</td>");
        rows.append("<td style='background-color: #f3f4f6; font-weight:bold;'>").append(total).append("</td>");
        rows.append("<td>").append(safe(s.getGrade())).append("</td>");
    }


    private int safe(Integer v) {
        return v == null ? 0 : v;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
