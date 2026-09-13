package com.academic.service;

import com.academic.entity.Subject;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.SubjectMapper;
import com.academic.repository.SubjectRepository;
import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository repository;

    @Override
    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        log.info("Request received to create Subject with code: {}", request != null ? request.getResolvedCode() : null);

        validateRequest(request, false, null);

        String normalizedCode = request.getResolvedCode().trim();
        if (repository.existsBySubjectCodeIgnoreCaseAndIsDeletedFalse(normalizedCode)) {
            log.warn("Subject creation failed: Duplicate subject code '{}'", normalizedCode);
            throw new CustomException(
                    "Subject with code '" + normalizedCode + "' already exists",
                    "DUPLICATE_SUBJECT_CODE",
                    "Please provide a unique subject code"
            );
        }

        Subject entity = SubjectMapper.toEntity(request);
        Subject saved = repository.save(entity);

        log.info("Subject created successfully with id: {} and code: {}", saved.getId(), saved.getSubjectCode());
        return SubjectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        log.info("Request received to update Subject with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid subject id provided for update: {}", id);
            throw new CustomException("Invalid subject id provided", "INVALID_ID", "Subject id must be a positive integer");
        }

        Subject existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Subject update failed: Subject not found with id: {}", id);
                    return new ResourceNotFoundException("Subject not found with id: " + id);
                });

        validateRequest(request, true, id);

        String normalizedCode = request.getResolvedCode().trim();
        if (repository.existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(normalizedCode, id)) {
            log.warn("Subject update failed: Duplicate subject code '{}' already in use", normalizedCode);
            throw new CustomException(
                    "Subject code '" + normalizedCode + "' is already in use by another course",
                    "DUPLICATE_SUBJECT_CODE",
                    "Please choose a different subject code"
            );
        }

        SubjectMapper.updateEntity(existing, request);
        Subject updated = repository.save(existing);

        log.info("Subject updated successfully with id: {} and code: {}", updated.getId(), updated.getSubjectCode());
        return SubjectMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Request received to soft delete Subject with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid subject id provided for deletion: {}", id);
            throw new CustomException("Invalid subject id provided", "INVALID_ID", "Subject id must be a positive integer");
        }

        Subject existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Subject deletion failed: Subject not found with id: {}", id);
                    return new ResourceNotFoundException("Subject not found with id: " + id);
                });

        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("Subject soft-deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        log.info("Fetching Subject by id: {}", id);

        if (id == null || id <= 0) {
            throw new CustomException("Invalid subject id provided", "INVALID_ID", "Subject id must be a positive integer");
        }

        Subject entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Subject not found with id: {}", id);
                    return new ResourceNotFoundException("Subject not found with id: " + id);
                });

        return SubjectMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllActive() {
        log.info("Fetching all active Subjects");
        List<Subject> list = repository.findByIsDeletedFalseAndStatusOrderBySubjectCodeAsc("Active");
        return SubjectMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getByProgram(String program) {
        log.info("Fetching Subjects by program: {}", program);
        if (program == null || program.isBlank()) {
            throw new CustomException("Program is required", "REQUIRED_FIELD", "program cannot be empty");
        }
        List<Subject> list = repository.findByProgramIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(program.trim());
        return SubjectMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getByDepartment(String department) {
        log.info("Fetching Subjects by department: {}", department);
        if (department == null || department.isBlank()) {
            throw new CustomException("Department is required", "REQUIRED_FIELD", "department cannot be empty");
        }
        List<Subject> list = repository.findByDepartmentIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(department.trim());
        return SubjectMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectResponse> getAll(
            int page,
            int size,
            String search,
            String department,
            String program,
            String semester,
            String academicYear,
            String type,
            String status,
            Integer credits
    ) {
        log.info("Fetching paginated Subjects - page: {}, size: {}, search: {}, dept: {}, program: {}, sem: {}, type: {}, status: {}",
                page, size, search, department, program, semester, type, status);

        if (page < 0) page = 0;
        if (size <= 0 || size > 200) size = 10;

        Pageable pageable = PageRequest.of(page, size);

        String cleanSearch = (search != null && !search.trim().isBlank()) ? search.trim() : null;
        String cleanDept = (department != null && !department.trim().isBlank() && !department.equalsIgnoreCase("all")) ? department.trim() : null;
        String cleanProgram = (program != null && !program.trim().isBlank() && !program.equalsIgnoreCase("all")) ? program.trim() : null;
        String cleanSem = (semester != null && !semester.trim().isBlank() && !semester.equalsIgnoreCase("all")) ? semester.trim() : null;
        String cleanYear = (academicYear != null && !academicYear.trim().isBlank() && !academicYear.equalsIgnoreCase("all")) ? academicYear.trim() : null;
        String cleanType = (type != null && !type.trim().isBlank() && !type.equalsIgnoreCase("all")) ? type.trim() : null;
        String cleanStatus = (status != null && !status.trim().isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<Subject> pageResult = repository.searchAndFilter(
                cleanSearch, cleanDept, cleanProgram, cleanSem, cleanYear, cleanType, cleanStatus, credits, pageable
        );
        return pageResult.map(SubjectMapper::toResponse);
    }

    private void validateRequest(SubjectRequest request, boolean isUpdate, Long existingId) {
        if (request == null) {
            log.warn("Validation failed: SubjectRequest body is null");
            throw new CustomException("Request body cannot be null", "INVALID_REQUEST", "Please provide a valid subject payload");
        }

        String resolvedCode = request.getResolvedCode();
        if (resolvedCode == null || resolvedCode.isBlank()) {
            log.warn("Validation failed: Subject code is missing");
            throw new CustomException("Subject code is required", "REQUIRED_FIELD", "Field 'code' / 'subjectCode' cannot be empty");
        }

        String resolvedName = request.getResolvedName();
        if (resolvedName == null || resolvedName.isBlank()) {
            log.warn("Validation failed: Subject name is missing");
            throw new CustomException("Subject name is required", "REQUIRED_FIELD", "Field 'name' / 'subjectName' cannot be empty");
        }

        if (request.getHrsPerWeek() != null && request.getHrsPerWeek() < 0) {
            log.warn("Validation failed: Invalid hrsPerWeek {}", request.getHrsPerWeek());
            throw new CustomException("Teaching hours per week must be zero or positive", "INVALID_HOURS", "Field 'hrsPerWeek' must be >= 0");
        }

        if (request.getCredits() != null && request.getCredits() < 0) {
            log.warn("Validation failed: Invalid credits {}", request.getCredits());
            throw new CustomException("Credits must be zero or positive", "INVALID_CREDITS", "Field 'credits' must be >= 0");
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String status = request.getStatus().trim();
            if (!status.equalsIgnoreCase("Active") && !status.equalsIgnoreCase("Inactive") && !status.equalsIgnoreCase("Draft")) {
                log.warn("Validation failed: Invalid status value '{}'", status);
                throw new CustomException("Status must be 'Active', 'Inactive', or 'Draft'", "INVALID_STATUS", "Invalid value for status");
            }
        }
    }
}
