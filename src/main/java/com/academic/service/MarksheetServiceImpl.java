package com.academic.service;

import com.academic.dto.MarksheetRequest;
import com.academic.entity.Marksheet;
import com.academic.entity.MarksheetSubjectMarks;
import com.academic.entity.Student;
import com.academic.repository.*;
import com.academic.response.MarksheetDetailResponse;
import com.academic.response.MarksheetResponse;
import com.academic.response.StandardResponse;
import com.academic.response.SubjectMarksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarksheetServiceImpl implements MarksheetService {

    private final MarksheetRepository marksheetRepo;
    private final MarksheetSubjectMarksRepository subjectRepo;
    private final CommonMasterRepository commonRepo;
    private final SessionRepository sessionRepo;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectMasterRepo;

    /* CREATE */
    @Transactional
    public StandardResponse<?> create(MarksheetRequest request) {
        return saveInternal(null, request);
    }

    /* UPDATE (SAME AS SAVE) */
    @Transactional
    public StandardResponse<?> update(Long id, MarksheetRequest request) {
        return saveInternal(id, request);
    }

    private StandardResponse<?> saveInternal(Long id, MarksheetRequest request) {

        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            return StandardResponse.error(
                    "Subjects are required",
                    "NO_SUBJECTS",
                    "subjects",
                    "At least one subject is mandatory");
        }

        Marksheet sheet;

        if (id == null) {
            sheet = new Marksheet();
        } else {
            sheet = marksheetRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Marksheet not found"));
            subjectRepo.deleteByMarksheetId(id); // 🔥 RESET SUBJECTS
        }

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
        sheet.setGpa(request.getGpa());

        sheet.setAttendanceDays(request.getAttendanceDays());
        sheet.setTotalWorkingDays(request.getTotalWorkingDays());
        sheet.setConductGrade(request.getConductGrade());
        sheet.setSportsGrade(request.getSportsGrade());
        sheet.setExtraCurricularGrade(request.getExtraCurricularGrade());
        sheet.setTeacherRemarks(request.getTeacherRemarks());
        sheet.setPrincipalRemarks(request.getPrincipalRemarks());

        marksheetRepo.save(sheet);

        request.getSubjects().forEach(s -> {

            int total = safe(s.getTheoryMarks()) +
                    safe(s.getPracticalMarks()) +
                    safe(s.getInternalMarks());

            int totalMax = safe(s.getTheoryMax()) +
                    safe(s.getPracticalMax()) +
                    safe(s.getInternalMax());

            subjectRepo.save(
                    MarksheetSubjectMarks.builder()
                            .marksheetId(sheet.getId())
                            .subjectId(s.getSubjectId())
                            .theoryMarks(s.getTheoryMarks())
                            .theoryMax(s.getTheoryMax())
                            .practicalMarks(s.getPracticalMarks())
                            .practicalMax(s.getPracticalMax())
                            .internalMarks(s.getInternalMarks())
                            .internalMax(s.getInternalMax())
                            .totalMarks(total)
                            .totalMax(totalMax)
                            .subjectRemarks(s.getSubjectRemarks())
                            .build());
        });

        return StandardResponse.success(
                sheet.getId(),
                id == null ? "Marksheet created" : "Marksheet updated");
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

    public StandardResponse<?> getById(Long id) {

        Marksheet sheet = marksheetRepo.findById(id).orElse(null);

        if (sheet == null || Boolean.TRUE.equals(sheet.getIsDeleted())) {
            return StandardResponse.error(
                    "Marksheet not found",
                    "NOT_FOUND",
                    "id",
                    "Invalid marksheet id");
        }

        return StandardResponse.success(mapToDetailResponse(sheet), "Marksheet fetched");
    }

    private MarksheetResponse mapToResponse(Marksheet sheet) {
      Student student=  studentRepository.findById(sheet.getStudentId()).get();
        return MarksheetResponse.builder()
                .id(sheet.getId())
                .studentId(sheet.getStudentId())
                .studentName(student.getFirstName()+ " "+student.getLastName() ) // placeholder if no student repo
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
        Student student=  studentRepository.findById(sheet.getStudentId()).get();
        List<SubjectMarksResponse> subjects = subjectRepo.findByMarksheetId(sheet.getId())
                .stream()
                .map(s -> {
                    SubjectMarksResponse r = new SubjectMarksResponse();
                    r.setId(s.getId());
                    r.setSubjectId(s.getSubjectId());
                    r.setSubjectName(getSubjectName(s.getSubjectId()));
                    r.setTheoryMarks(s.getTheoryMarks());
                    r.setTheoryMax(s.getTheoryMax());
                    r.setPracticalMarks(s.getPracticalMarks());
                    r.setPracticalMax(s.getPracticalMax());
                    r.setInternalMarks(s.getInternalMarks());
                    r.setInternalMax(s.getInternalMax());
                    r.setTotalMarks(s.getTotalMarks());
                    r.setTotalMax(s.getTotalMax());
                    r.setSubjectRemarks(s.getSubjectRemarks());
                    return r;
                })
                .toList();

        MarksheetDetailResponse response = new MarksheetDetailResponse();

        response.setId(sheet.getId());
        response.setStudentId(sheet.getStudentId());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());

        response.setClassId(sheet.getClassId());
        response.setClassName(getName(sheet.getClassId()));

        response.setSectionId(sheet.getSectionId());
        response.setSectionName(getName(sheet.getSectionId()));

        response.setSessionId(sheet.getSessionId());
        response.setSessionName(getSessionName(sheet.getSessionId()));

        response.setExamTypeId(sheet.getExamTypeId());
        response.setExamTypeName(getName(sheet.getExamTypeId()));

        response.setExamDate(sheet.getExamDate());
        response.setSubjects(subjects);

        response.setTotalMarksObtained(sheet.getTotalMarksObtained());
        response.setTotalMaxMarks(sheet.getTotalMaxMarks());
        response.setPercentage(sheet.getPercentage());
        response.setGrade(sheet.getGrade());
        response.setGpa(sheet.getGpa());

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

    private String getSubjectName(Long id) {
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
}
