package com.academic.service;

import com.academic.dto.CollegeMarksheetResponse;
import com.academic.dto.CollegeMarksheetSubjectResponse;
import com.academic.dto.ExcelValidationError;
import com.academic.dto.GazetteUploadResponse;
import com.academic.entity.CollegeMarksheet;
import com.academic.entity.CollegeMarksheetSubject;
import com.academic.entity.Student;
import com.academic.entity.Degree;
import com.academic.entity.Program;
import com.academic.exception.ResourceNotFoundException;
import com.academic.repository.CollegeMarksheetRepository;
import com.academic.repository.DegreeRepository;
import com.academic.repository.ProgramRepository;
import com.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeMarksheetServiceImpl implements CollegeMarksheetService {

    private final CollegeMarksheetRepository repository;
    private final CollegeMarksheetExcelService excelService;
    private final StudentRepository studentRepository;
    private final DegreeRepository degreeRepository;
    private final ProgramRepository programRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CollegeMarksheetResponse> getAllMarksheets(
            String degreeCode,
            String programCode,
            String semester,
            String academicYear,
            String examSession,
            String resultStatus,
            String search,
            Pageable pageable
    ) {
        String cleanDegree = (degreeCode != null && !degreeCode.trim().equalsIgnoreCase("all")) ? degreeCode.trim() : null;
        String cleanProgram = (programCode != null && !programCode.trim().equalsIgnoreCase("all")) ? programCode.trim() : null;
        String cleanSemester = (semester != null && !semester.trim().equalsIgnoreCase("all")) ? semester.trim() : null;
        String cleanYear = (academicYear != null && !academicYear.trim().equalsIgnoreCase("all")) ? academicYear.trim() : null;
        String cleanSession = (examSession != null && !examSession.trim().equalsIgnoreCase("all")) ? examSession.trim() : null;
        String cleanStatus = (resultStatus != null && !resultStatus.trim().equalsIgnoreCase("all")) ? resultStatus.trim() : null;
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Integer degreeId = null;
        if (cleanDegree != null) {
            try {
                degreeId = Integer.parseInt(cleanDegree);
            } catch (NumberFormatException ignored) {}
        }

        Integer programId = null;
        if (cleanProgram != null) {
            try {
                programId = Integer.parseInt(cleanProgram);
            } catch (NumberFormatException ignored) {}
        }

        Page<CollegeMarksheet> page = repository.searchAndFilter(
                degreeId, cleanDegree, programId, cleanProgram, cleanSemester, cleanYear, cleanSession, cleanStatus, cleanSearch, pageable
        );

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeMarksheetResponse getById(Long id) {
        CollegeMarksheet ms = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College Marksheet not found with id: " + id));
        return mapToResponse(ms);
    }

    @Override
    @Transactional
    public GazetteUploadResponse uploadAndIngestGazette(
            MultipartFile file,
            String degreeCode,
            String programCode,
            String semester,
            String academicYear,
            String examSession
    ) {
        if (file == null || file.isEmpty()) {
            return GazetteUploadResponse.builder()
                    .success(false)
                    .message("Uploaded file is empty or missing.")
                    .errors(List.of(new ExcelValidationError(0, "FILE", "", "File is empty.")))
                    .build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            return GazetteUploadResponse.builder()
                    .success(false)
                    .message("Invalid file format. Please upload an Excel (.xlsx or .xls) file.")
                    .errors(List.of(new ExcelValidationError(0, "FORMAT", fileName, "Only Excel format is accepted.")))
                    .build();
        }

        try (InputStream is = file.getInputStream()) {
            CollegeMarksheetExcelService.ParsingResult result = excelService.parseAndValidateExcel(
                    is, degreeCode, programCode, semester, academicYear, examSession
            );

            if (!result.errors.isEmpty()) {
                return GazetteUploadResponse.builder()
                        .success(false)
                        .message("Validation failed with " + result.errors.size() + " error(s). Please fix the issues and re-upload.")
                        .totalRowsProcessed(result.totalRows)
                        .errorCount(result.errors.size())
                        .errors(result.errors)
                        .build();
            }

            // Save or Update Marksheets in Database
            int savedCount = 0;
            for (CollegeMarksheet newMs : result.marksheets) {
                Optional<CollegeMarksheet> existingOpt = repository
                        .findByUniversityPrnAndExamSessionAndSemesterAndIsDeletedFalse(
                                newMs.getUniversityPrn(), newMs.getExamSession(), newMs.getSemester()
                        );

                if (existingOpt.isPresent()) {
                    CollegeMarksheet existing = existingOpt.get();
                    // Update existing marksheet with new results
                    existing.setStudentId(newMs.getStudentId());
                    existing.setAdmissionNo(newMs.getAdmissionNo());
                    existing.setStudentName(newMs.getStudentName());
                    existing.setCollegeRollNo(newMs.getCollegeRollNo());
                    existing.setFatherName(newMs.getFatherName());
                    existing.setDegree(newMs.getDegree());
                    existing.setProgram(newMs.getProgram());
                    existing.setTotalCreditsOffered(newMs.getTotalCreditsOffered());
                    existing.setTotalCreditsEarned(newMs.getTotalCreditsEarned());
                    existing.setSgpa(newMs.getSgpa());
                    existing.setCgpa(newMs.getCgpa());
                    existing.setTotalMarksObtained(newMs.getTotalMarksObtained());
                    existing.setTotalMaxMarks(newMs.getTotalMaxMarks());
                    existing.setPercentage(newMs.getPercentage());
                    existing.setResultStatus(newMs.getResultStatus());
                    existing.setBacklogCount(newMs.getBacklogCount());

                    // Clear old subjects and replace
                    existing.getSubjects().clear();
                    for (CollegeMarksheetSubject sub : newMs.getSubjects()) {
                        sub.setCollegeMarksheet(existing);
                        existing.getSubjects().add(sub);
                    }
                    repository.save(existing);
                } else {
                    repository.save(newMs);
                }
                savedCount++;
            }

            String statusMsg;
            if (result.unlinkedCount > 0 && result.linkedCount > 0) {
                statusMsg = String.format("Successfully ingested %d student marksheets (%d linked with student database, %d unlinked - pending student registration).",
                        savedCount, result.linkedCount, result.unlinkedCount);
            } else if (result.unlinkedCount > 0) {
                statusMsg = String.format("Successfully ingested %d student marksheets (%d students pending registration in college student database).",
                        savedCount, result.unlinkedCount);
            } else {
                statusMsg = String.format("Successfully ingested and linked all %d student marksheets (%d course entries).",
                        savedCount, result.totalSubjects);
            }

            return GazetteUploadResponse.builder()
                    .success(true)
                    .message(statusMsg)
                    .totalRowsProcessed(result.totalRows)
                    .validStudentsCount(savedCount)
                    .linkedStudentsCount(result.linkedCount)
                    .unlinkedStudentsCount(result.unlinkedCount)
                    .totalSubjectsParsed(result.totalSubjects)
                    .errorCount(0)
                    .errors(Collections.emptyList())
                    .build();

        } catch (Exception e) {
            log.error("Failed to process Gazette upload", e);
            return GazetteUploadResponse.builder()
                    .success(false)
                    .message("Processing error: " + e.getMessage())
                    .errors(List.of(new ExcelValidationError(0, "SYSTEM", "", e.getMessage())))
                    .build();
        }
    }

    @Override
    public byte[] getExcelTemplate() {
        return excelService.generateExcelTemplate();
    }

    @Override
    @Transactional
    public boolean togglePublish(Long id, Boolean published) {
        CollegeMarksheet ms = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marksheet not found with id: " + id));
        ms.setPublished(published != null ? published : !Boolean.TRUE.equals(ms.getPublished()));
        repository.save(ms);
        return ms.getPublished();
    }

    @Override
    @Transactional
    public int batchPublish(List<Long> ids, Boolean published) {
        if (ids == null || ids.isEmpty()) return 0;
        int count = 0;
        for (Long id : ids) {
            Optional<CollegeMarksheet> opt = repository.findByIdAndIsDeletedFalse(id);
            if (opt.isPresent()) {
                CollegeMarksheet ms = opt.get();
                ms.setPublished(Boolean.TRUE.equals(published));
                repository.save(ms);
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional
    public void deleteMarksheet(Long id) {
        CollegeMarksheet ms = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marksheet not found with id: " + id));
        ms.setIsDeleted(true);
        repository.save(ms);
    }

    @Override
    @Transactional
    public int autoLinkUnlinkedMarksheets() {
        List<CollegeMarksheet> unlinked = repository.findByStudentIdIsNullAndIsDeletedFalse();
        if (unlinked.isEmpty()) return 0;

        Set<String> admissionNos = unlinked.stream()
                .map(CollegeMarksheet::getAdmissionNo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (admissionNos.isEmpty()) return 0;

        List<Student> dbStudents = studentRepository.findByAdmissionNoInIgnoreCase(admissionNos);
        Map<String, Student> studentMap = dbStudents.stream()
                .filter(s -> s.getAdmissionNo() != null)
                .collect(Collectors.toMap(s -> s.getAdmissionNo().trim().toLowerCase(), s -> s, (a, b) -> a));

        int linked = 0;
        for (CollegeMarksheet ms : unlinked) {
            if (ms.getAdmissionNo() != null && studentMap.containsKey(ms.getAdmissionNo().trim().toLowerCase())) {
                Student s = studentMap.get(ms.getAdmissionNo().trim().toLowerCase());
                ms.setStudentId(s.getId().longValue());
                if (ms.getFatherName() == null) ms.setFatherName(s.getFatherName());
                if (ms.getMotherName() == null) ms.setMotherName(s.getMotherName());
                if (ms.getDegree() == null && s.getDegree() != null) {
                    degreeRepository.findByIdAndIsDeletedFalse(s.getDegree()).ifPresent(ms::setDegree);
                }
                if (ms.getProgram() == null && s.getBranch() != null) {
                    programRepository.findByIdAndIsDeletedFalse(s.getBranch()).ifPresent(ms::setProgram);
                }
                repository.save(ms);
                linked++;
            }
        }
        if (linked > 0) {
            log.info("Auto-linked {} previously unlinked marksheets to registered students", linked);
        }
        return linked;
    }

    private CollegeMarksheetResponse mapToResponse(CollegeMarksheet entity) {
        List<CollegeMarksheetSubjectResponse> subjectResponses = new ArrayList<>();
        if (entity.getSubjects() != null) {
            subjectResponses = entity.getSubjects().stream().map(sub ->
                    CollegeMarksheetSubjectResponse.builder()
                            .id(sub.getId())
                            .subjectCode(sub.getSubjectCode())
                            .subjectName(sub.getSubjectName())
                            .subjectType(sub.getSubjectType())
                            .credits(sub.getCredits())
                            .internalMaxMarks(sub.getInternalMaxMarks())
                            .internalObtainedMarks(sub.getInternalObtainedMarks())
                            .externalMaxMarks(sub.getExternalMaxMarks())
                            .externalObtainedMarks(sub.getExternalObtainedMarks())
                            .practicalMaxMarks(sub.getPracticalMaxMarks())
                            .practicalObtainedMarks(sub.getPracticalObtainedMarks())
                            .totalMarks(sub.getTotalMarks())
                            .maxTotalMarks(sub.getMaxTotalMarks())
                            .gradePoint(sub.getGradePoint())
                            .letterGrade(sub.getLetterGrade())
                            .status(sub.getStatus())
                            .isBacklog(sub.getIsBacklog())
                            .attemptNumber(sub.getAttemptNumber())
                            .build()
            ).collect(Collectors.toList());
        }

        Integer degreeId = entity.getDegreeId();
        String degreeCode = entity.getDegreeCode();
        Integer programId = entity.getProgramId();
        String programCode = entity.getProgramCode();
        String programName = entity.getProgramName();

        // Robust self-healing / Fallback from student record if null
        if (degreeId == null || programId == null || degreeCode == null || programCode == null) {
            Student s = null;
            if (entity.getStudentId() != null) {
                s = studentRepository.findByIdAndIsDeletedFalse(entity.getStudentId().intValue()).orElse(null);
            }
            if (s == null && entity.getAdmissionNo() != null && !entity.getAdmissionNo().trim().isEmpty()) {
                s = studentRepository.findByAdmissionNoAndIsDeletedFalse(entity.getAdmissionNo().trim()).orElse(null);
            }

            if (s != null) {
                if (degreeId == null && s.getDegree() != null) {
                    degreeId = s.getDegree();
                }
                if (programId == null && s.getBranch() != null) {
                    programId = s.getBranch();
                }
            }

            if (degreeCode == null && degreeId != null) {
                Degree d = degreeRepository.findByIdAndIsDeletedFalse(degreeId).orElse(null);
                if (d != null) {
                    degreeCode = d.getCode();
                }
            } else if (degreeId == null && degreeCode != null && !degreeCode.isBlank()) {
                Degree d = degreeRepository.findByCodeIgnoreCaseAndIsDeletedFalse(degreeCode.trim()).orElse(null);
                if (d != null) {
                    degreeId = d.getId();
                }
            }

            if (programCode == null && programId != null) {
                Program p = programRepository.findByIdAndIsDeletedFalse(programId).orElse(null);
                if (p != null) {
                    programCode = p.getCode();
                    programName = p.getName();
                }
            } else if (programId == null && programCode != null && !programCode.isBlank()) {
                Program p = programRepository.findByCodeIgnoreCaseAndIsDeletedFalse(programCode.trim()).orElse(null);
                if (p != null) {
                    programId = p.getId();
                    if (programName == null) programName = p.getName();
                }
            }
        }

        return CollegeMarksheetResponse.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .studentName(entity.getStudentName())
                .universityPrn(entity.getUniversityPrn())
                .universityRollNo(entity.getUniversityRollNo())
                .collegeRollNo(entity.getCollegeRollNo())
                .admissionNo(entity.getAdmissionNo())
                .fatherName(entity.getFatherName())
                .motherName(entity.getMotherName())
                .degreeId(degreeId)
                .degreeCode(degreeCode)
                .programId(programId)
                .programCode(programCode)
                .programName(programName)
                .departmentName(entity.getDepartmentName())
                .academicYear(entity.getAcademicYear())
                .semester(entity.getSemester())
                .examSession(entity.getExamSession())
                .examType(entity.getExamType())
                .totalCreditsOffered(entity.getTotalCreditsOffered())
                .totalCreditsEarned(entity.getTotalCreditsEarned())
                .sgpa(entity.getSgpa())
                .cgpa(entity.getCgpa())
                .totalMarksObtained(entity.getTotalMarksObtained())
                .totalMaxMarks(entity.getTotalMaxMarks())
                .percentage(entity.getPercentage())
                .resultStatus(entity.getResultStatus())
                .backlogCount(entity.getBacklogCount())
                .marksheetNumber(entity.getMarksheetNumber())
                .issueDate(entity.getIssueDate())
                .verificationCode(entity.getVerificationCode())
                .published(entity.getPublished())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .subjects(subjectResponses)
                .build();
    }
}
