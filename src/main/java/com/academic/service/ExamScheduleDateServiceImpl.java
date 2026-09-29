package com.academic.service;

import com.academic.entity.*;
import com.academic.repository.*;
import com.academic.request.ExamScheduleDateRequest;
import com.academic.response.ExamScheduleDateResponse;
import com.academic.response.StandardResponse;
import com.academic.utility.Template;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamScheduleDateServiceImpl implements ExamScheduleDateService {

    private final ExamScheduleDateRepository repository;
    private final ExamScheduleRepository examScheduleRepository;
    private final SessionRepository sessionRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final SubjectRepository subjectRepository;
    private final StaffRepository staffRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);

    @Override
    public StandardResponse<?> save(ExamScheduleDateRequest request) {
        if (request.getClassId() == null) {
            return StandardResponse.error("Class ID is required", "BAD_REQUEST", null);
        }
        if (request.getSubjectId() == null) {
            return StandardResponse.error("Subject ID is required", "BAD_REQUEST", null);
        }
        if (request.getExamDate() == null) {
            return StandardResponse.error("Exam date is required", "BAD_REQUEST", null);
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            return StandardResponse.error("Start time is required", "BAD_REQUEST", null);
        }
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            return StandardResponse.error("End time is required", "BAD_REQUEST", null);
        }

        ExamScheduleDate entity;
        if (request.getId() != null) {
            entity = repository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Exam schedule date not found with id: " + request.getId()));
        } else {
            entity = new ExamScheduleDate();
        }

        mapRequestToEntity(request, entity);
        ExamScheduleDate saved = repository.save(entity);

        return StandardResponse.success(mapToResponse(saved), "Exam schedule date saved successfully");
    }

    @Override
    public StandardResponse<?> bulkSave(List<ExamScheduleDateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return StandardResponse.error("Request list cannot be empty", "BAD_REQUEST", null);
        }

        List<ExamScheduleDateResponse> responses = new ArrayList<>();
        for (ExamScheduleDateRequest req : requests) {
            ExamScheduleDate entity;
            if (req.getId() != null) {
                entity = repository.findById(req.getId())
                        .orElse(new ExamScheduleDate());
            } else {
                entity = new ExamScheduleDate();
            }

            mapRequestToEntity(req, entity);
            ExamScheduleDate saved = repository.save(entity);
            responses.add(mapToResponse(saved));
        }

        return StandardResponse.success(responses, "Exam schedule dates saved successfully in bulk");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getById(Long id) {
        ExamScheduleDate entity = repository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Exam schedule date not found with id: " + id));

        return StandardResponse.success(mapToResponse(entity), "Exam schedule date fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getAll(
            Long examScheduleId,
            Integer sessionId,
            Integer classId,
            Integer sectionId,
            LocalDate examDate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ExamScheduleDate> list = repository.filterExamSchedules(
                examScheduleId, sessionId, classId, sectionId, examDate, startDate, endDate
        );

        List<ExamScheduleDateResponse> responseList = list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return StandardResponse.success(responseList, "Exam schedule dates fetched successfully");
    }

    @Override
    public StandardResponse<?> delete(Long id) {
        ExamScheduleDate entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam schedule date not found with id: " + id));

        entity.setIsDeleted(true);
        entity.setIsActive(false);
        repository.save(entity);

        return StandardResponse.success(null, "Exam schedule date deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateExamTimeTablePdf(
            Long examScheduleId,
            Integer sessionId,
            Integer classId,
            Integer sectionId,
            LocalDate examDate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ExamScheduleDate> list = repository.filterExamSchedules(
                examScheduleId, sessionId, classId, sectionId, examDate, startDate, endDate
        );

        String sessionName = "Current Session";
        String examTitle = "Annual / Periodic Examination";
        String classSectionStr = "All Classes";
        String dateRangeStr = "All Dates";

        if (classId != null) {
            classSectionStr = commonMasterRepository.findById(classId)
                    .map(CommonMaster::getData)
                    .orElse("Class ID: " + classId);

            if (sectionId != null) {
                String sec = commonMasterRepository.findById(sectionId)
                        .map(CommonMaster::getData)
                        .orElse("");
                if (!sec.isEmpty()) {
                    classSectionStr += " - " + sec;
                }
            }
        }

        if (examScheduleId != null) {
            ExamSchedule es = examScheduleRepository.findById(examScheduleId).orElse(null);
            if (es != null) {
                examTitle = es.getExamTitle() != null ? es.getExamTitle() : examTitle;
                if (es.getSession() != null) {
                    sessionName = es.getSession().getSession();
                }
            }
        } else if (sessionId != null) {
            sessionName = sessionRepository.findById(sessionId)
                    .map(Session::getSession)
                    .orElse(sessionName);
        }

        if (examDate != null) {
            dateRangeStr = examDate.format(DATE_FORMAT);
        } else if (startDate != null && endDate != null) {
            dateRangeStr = startDate.format(DATE_FORMAT) + " to " + endDate.format(DATE_FORMAT);
        } else if (startDate != null) {
            dateRangeStr = "From " + startDate.format(DATE_FORMAT);
        } else if (endDate != null) {
            dateRangeStr = "Up to " + endDate.format(DATE_FORMAT);
        }

        StringBuilder tableRows = new StringBuilder();
        if (list.isEmpty()) {
            tableRows.append("<tr><td colspan=\"7\" style=\"text-align:center; padding: 20px; color:#64748b;\">No exam schedule records found for the selected criteria.</td></tr>");
        } else {
            int index = 1;
            for (ExamScheduleDate item : list) {
                String subName = item.getSubject() != null ? item.getSubject().getSubjectName() : "-";
                String subCode = (item.getSubject() != null && item.getSubject().getSubjectCode() != null)
                        ? " (" + item.getSubject().getSubjectCode() + ")" : "";
                String fullSubject = escapeHtml(subName + subCode);

                String dateStr = item.getExamDate() != null ? item.getExamDate().format(DATE_FORMAT) : "-";
                String dayStr = item.getExamDate() != null ? item.getExamDate().format(DAY_FORMAT) : "-";
                String timeSlot = escapeHtml(item.getStartTime() + " - " + item.getEndTime());
                String room = item.getRoomNo() != null ? escapeHtml(item.getRoomNo()) : "-";
                String invigilator = item.getInvigilatorName() != null ? escapeHtml(item.getInvigilatorName()) : "-";

                tableRows.append("<tr>")
                        .append("<td>").append(index++).append("</td>")
                        .append("<td class=\"date-cell\">").append(dateStr).append("</td>")
                        .append("<td>").append(dayStr).append("</td>")
                        .append("<td class=\"subject-cell\">").append(fullSubject).append("</td>")
                        .append("<td class=\"time-cell\">").append(timeSlot).append("</td>")
                        .append("<td>").append(room).append("</td>")
                        .append("<td>").append(invigilator).append("</td>")
                        .append("</tr>");
            }
        }

        String printDate = LocalDate.now().format(DATE_FORMAT);

        String html = Template.EXAM_TIMETABLE_PDF_HTML
                .replace("${EXAM_TITLE}", escapeHtml(examTitle))
                .replace("${SESSION}", escapeHtml(sessionName))
                .replace("${CLASS_SECTION}", escapeHtml(classSectionStr))
                .replace("${DATE_RANGE}", escapeHtml(dateRangeStr))
                .replace("${PRINT_DATE}", printDate)
                .replace("${TABLE_ROWS}", tableRows.toString());

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for exam timetable", e);
            throw new RuntimeException("Exam Timetable PDF generation failed: " + e.getMessage(), e);
        }
    }

    private void mapRequestToEntity(ExamScheduleDateRequest request, ExamScheduleDate entity) {
        if (request.getExamScheduleId() != null) {
            ExamSchedule examSchedule = examScheduleRepository.findById(request.getExamScheduleId()).orElse(null);
            entity.setExamSchedule(examSchedule);
            if (examSchedule != null && examSchedule.getSession() != null && entity.getSession() == null) {
                entity.setSession(examSchedule.getSession());
            }
        }

        if (request.getSessionId() != null) {
            Session session = sessionRepository.findById(request.getSessionId()).orElse(null);
            entity.setSession(session);
        }

        if (request.getClassId() != null) {
            CommonMaster classMaster = commonMasterRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("Invalid Class ID: " + request.getClassId()));
            entity.setClassMaster(classMaster);
        }

        if (request.getSectionId() != null) {
            CommonMaster sectionMaster = commonMasterRepository.findById(request.getSectionId()).orElse(null);
            entity.setSectionMaster(sectionMaster);
        }

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findByIdAndIsDeletedFalse(request.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Invalid Subject ID: " + request.getSubjectId()));
            entity.setSubject(subject);
        }

        if (request.getInvigilatorId() != null) {
            Staff staff = staffRepository.findByIdAndIsDeletedFalse(request.getInvigilatorId()).orElse(null);
            entity.setInvigilator(staff);
            if (staff != null && (request.getInvigilatorName() == null || request.getInvigilatorName().trim().isEmpty())) {
                entity.setInvigilatorName(staff.getFirstName() + " " + staff.getLastName());
            }
        }

        if (request.getInvigilatorName() != null && !request.getInvigilatorName().trim().isEmpty()) {
            entity.setInvigilatorName(request.getInvigilatorName());
        }

        entity.setExamDate(request.getExamDate());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setRoomNo(request.getRoomNo());
        entity.setMaxMarks(request.getMaxMarks());
        entity.setPassingMarks(request.getPassingMarks());

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            entity.setStatus(request.getStatus());
        } else if (entity.getStatus() == null) {
            entity.setStatus("SCHEDULED");
        }

        entity.setIsDeleted(false);
        entity.setIsActive(true);
    }

    private ExamScheduleDateResponse mapToResponse(ExamScheduleDate e) {
        String examTitle = e.getExamSchedule() != null ? e.getExamSchedule().getExamTitle() : null;
        String sessionStr = e.getSession() != null ? e.getSession().getSession() : null;

        String className = e.getClassMaster() != null ? e.getClassMaster().getData() : null;
        String sectionName = e.getSectionMaster() != null ? e.getSectionMaster().getData() : null;

        String subjectName = e.getSubject() != null ? e.getSubject().getSubjectName() : null;
        String subjectCode = e.getSubject() != null ? e.getSubject().getSubjectCode() : null;

        String formattedDate = e.getExamDate() != null ? e.getExamDate().format(DATE_FORMAT) : null;
        String dayOfWeek = e.getExamDate() != null ? e.getExamDate().format(DAY_FORMAT) : null;

        String timeRange = (e.getStartTime() != null ? e.getStartTime() : "") +
                (e.getEndTime() != null ? " - " + e.getEndTime() : "");

        String invigilatorName = e.getInvigilatorName();
        String invigilatorCode = null;
        if (e.getInvigilator() != null) {
            if (invigilatorName == null) {
                invigilatorName = e.getInvigilator().getFirstName() + " " + e.getInvigilator().getLastName();
            }
            invigilatorCode = e.getInvigilator().getStaffCode();
        }

        return ExamScheduleDateResponse.builder()
                .id(e.getId())
                .examScheduleId(e.getExamSchedule() != null ? e.getExamSchedule().getId() : null)
                .examScheduleTitle(examTitle)
                .sessionId(e.getSession() != null ? e.getSession().getId() : null)
                .session(sessionStr)
                .classId(e.getClassMaster() != null ? e.getClassMaster().getId() : null)
                .className(className)
                .sectionId(e.getSectionMaster() != null ? e.getSectionMaster().getId() : null)
                .sectionName(sectionName)
                .subjectId(e.getSubject() != null ? e.getSubject().getId() : null)
                .subjectName(subjectName)
                .subjectCode(subjectCode)
                .examDate(e.getExamDate())
                .formattedDate(formattedDate)
                .dayOfWeek(dayOfWeek)
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .timeRange(timeRange)
                .invigilatorId(e.getInvigilator() != null ? e.getInvigilator().getId() : null)
                .invigilatorName(invigilatorName)
                .invigilatorCode(invigilatorCode)
                .roomNo(e.getRoomNo())
                .maxMarks(e.getMaxMarks())
                .passingMarks(e.getPassingMarks())
                .status(e.getStatus())
                .isActive(e.getIsActive())
                .build();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
